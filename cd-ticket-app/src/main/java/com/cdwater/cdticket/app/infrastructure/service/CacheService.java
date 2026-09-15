package com.cdwater.cdticket.app.infrastructure.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存服务：逻辑过期 + 互斥锁（Redisson）异步重建，防缓存击穿。
 * 存储格式：value = {"d": <业务数据 JSON>, "e": <逻辑过期毫秒时间戳>}
 * 写入 TTL 叠加随机抖动，防缓存雪崩；loader 返回 null 时缓存空值（配合布隆过滤防穿透）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private static final String LOCK_PREFIX = "cache:lock:";
    private static final long LOCK_WAIT_MS = 200;
    private static final long LOCK_HOLD_SECONDS = 10;
    private static final long TTL_JITTER_SECONDS = 60;
    /** 空值缓存 TTL（d 为 null 即空值标记） */
    private static final long EMPTY_TTL_SECONDS = 300;

    private final StringRedisTemplate redis;
    private final RedissonClient redisson;
    private final ObjectMapper objectMapper;

    private final ExecutorService rebuildPool = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "cache-rebuild");
        t.setDaemon(true);
        return t;
    });

    /**
     * 读缓存；未命中或逻辑过期时重建。
     * - 未命中：互斥锁内同步重建（无旧值可返回）
     * - 逻辑过期：互斥锁内异步重建，本次返回旧值（击穿防御）
     */
    public <T> T getOrLoad(String key, long ttlSeconds, Supplier<T> loader, TypeReference<T> type) {
        String raw = redis.opsForValue().get(key);
        if (raw == null) {
            return rebuildSync(key, ttlSeconds, loader, type);
        }
        JsonNode node = parse(raw);
        if (node == null) {
            return rebuildSync(key, ttlSeconds, loader, type);
        }
        if (node.path("e").asLong(0) > System.currentTimeMillis()) {
            return readData(node, type);
        }
        // 逻辑过期：抢锁者异步重建，其余直接返回旧值
        RLock lock = redisson.getLock(LOCK_PREFIX + key);
        if (lock.tryLock()) {
            try {
                rebuildPool.submit(() -> {
                    try {
                        rebuildSync(key, ttlSeconds, loader, type);
                    } catch (Exception e) {
                        log.error("cache rebuild failed key={}", key, e);
                    }
                });
            } finally {
                lock.unlock();
            }
        }
        return readData(node, type);
    }

    /** 布隆过滤（Redis Bitmap 实现，m=65536 位，3 个哈希）；返回 true 表示"可能存在" */
    public boolean bloomContains(String bloomKey, long id) {
        boolean all = true;
        for (long bit : bloomBits(id)) {
            if (!Boolean.TRUE.equals(redis.opsForValue().getBit(bloomKey, bit))) {
                all = false;
            }
        }
        return all;
    }

    public void bloomAdd(String bloomKey, long id) {
        for (long bit : bloomBits(id)) {
            redis.opsForValue().setBit(bloomKey, bit, true);
        }
    }

    private static long[] bloomBits(long id) {
        return new long[]{id & 0xFFFF, (id * 31 + 7) & 0xFFFF, (id * 131 + 13) & 0xFFFF};
    }

    private <T> T rebuildSync(String key, long ttlSeconds, Supplier<T> loader, TypeReference<T> type) {
        RLock lock = redisson.getLock(LOCK_PREFIX + key);
        if (lock.tryLock()) {
            try {
                // 拿到锁后可能已被其他线程重建，先再读一次
                String raw = redis.opsForValue().get(key);
                if (raw != null) {
                    JsonNode node = parse(raw);
                    if (node != null && node.path("e").asLong(0) > System.currentTimeMillis()) {
                        return readData(node, type);
                    }
                }
                T data = loader.get();
                put(key, ttlSeconds, data);
                return data;
            } finally {
                lock.unlock();
            }
        }
        // 拿不到锁：等待并重读（重建方即将完成）
        sleepQuietly(LOCK_WAIT_MS);
        String raw = redis.opsForValue().get(key);
        if (raw != null) {
            T data = readData(parse(raw), type);
            if (data != null) {
                return data;
            }
        }
        return loader.get();
    }

    private <T> void put(String key, long ttlSeconds, T data) {
        long expireAt = System.currentTimeMillis() + (ttlSeconds + jitter()) * 1000;
        String json;
        try {
            json = objectMapper.writeValueAsString(
                    objectMapper.createObjectNode()
                            .put("e", expireAt)
                            .set("d", data == null ? objectMapper.nullNode() : objectMapper.valueToTree(data)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("cache serialize failed", e);
        }
        long ttl = data == null ? EMPTY_TTL_SECONDS + jitter() : ttlSeconds + jitter();
        redis.opsForValue().set(key, json, Duration.ofSeconds(ttl));
    }

    private static long jitter() {
        return ThreadLocalRandom.current().nextLong(TTL_JITTER_SECONDS);
    }

    private JsonNode parse(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private <T> T readData(JsonNode node, TypeReference<T> type) {
        JsonNode d = node.path("d");
        if (d.isNull() || d.isMissingNode()) {
            return null;
        }
        try {
            return objectMapper.readValue(d.toString(), type);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

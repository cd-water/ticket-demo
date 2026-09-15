package com.cdwater.cdticket.app.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 座位锁定：基于 Redis Bitmap 的原子检查与锁定。
 * - seat:lock:{screeningId}  锁定中座位位图（下单时置位，支付/取消/超时清位，TTL 15 分钟兜底）
 * - seat:sold:{screeningId}  已售座位位图（支付时置位，只增不减）
 * 位偏移 = (排-1) * 每排座位数 + (座-1)
 */
@Service
@RequiredArgsConstructor
public class SeatLockService {

    private static final String LOCK_KEY = "seat:lock:%d";
    private static final String SOLD_KEY = "seat:sold:%d";
    private static final long LOCK_TTL_SECONDS = 15 * 60;
    private static final String SOLD_INIT_KEY = "seat:sold:init:%d";

    private final StringRedisTemplate redis;

    /** 原子检查并锁定；返回冲突座位列表（空 = 全部锁定成功） */
    public List<int[]> lock(List<int[]> seats, Long screeningId, int seatCols) {
        List<Long> result = redis.execute(LOCK_SCRIPT, List.of(lockKey(screeningId), soldKey(screeningId)),
                buildArgs(seats, seatCols));
        if (result == null || result.isEmpty()) {
            redis.expire(lockKey(screeningId), java.time.Duration.ofSeconds(LOCK_TTL_SECONDS));
            return List.of();
        }
        return result.stream().map(off -> new int[]{off.intValue() / seatCols + 1, off.intValue() % seatCols + 1}).toList();
    }

    /** 释放锁定位（取消/超时关单） */
    public void release(List<int[]> seats, Long screeningId, int seatCols) {
        redis.execute(RELEASE_SCRIPT, List.of(lockKey(screeningId)), buildArgs(seats, seatCols));
    }

    /** 置已售位并清锁定位（支付成功） */
    public void markSold(List<int[]> seats, Long screeningId, int seatCols) {
        redis.execute(MARK_SOLD_SCRIPT, List.of(lockKey(screeningId), soldKey(screeningId)),
                buildArgs(seats, seatCols));
    }

    /** 已售位图是否已从 DB 初始化 */
    public boolean soldInitialized(Long screeningId) {
        return Boolean.TRUE.equals(redis.hasKey(String.format(SOLD_INIT_KEY, screeningId)));
    }

    /** 已售位图初始化完成标记（幂等，重复调用无害） */
    public void markSoldInitialized(Long screeningId) {
        redis.opsForValue().setIfAbsent(String.format(SOLD_INIT_KEY, screeningId), "1");
    }

    /** 查询座位状态：0 可用 / 1 已售 / 2 锁定中（模板禁用由调用方叠加为 3）。整图一次 GET，Java 侧解析 */
    public List<Integer> status(Long screeningId, List<int[]> seats, int seatCols) {
        byte[] lockBytes = getBitmap(lockKey(screeningId));
        byte[] soldBytes = getBitmap(soldKey(screeningId));
        List<Integer> result = new ArrayList<>(seats.size());
        for (int[] s : seats) {
            int off = (s[0] - 1) * seatCols + (s[1] - 1);
            boolean locked = bitAt(lockBytes, off);
            boolean isSold = bitAt(soldBytes, off);
            result.add(isSold ? 1 : locked ? 2 : 0);
        }
        return result;
    }

    /** 二进制安全读取位图（StringRedisTemplate 的 GET 按 UTF-8 解码会损坏高位字节） */
    private byte[] getBitmap(String key) {
        return redis.execute((org.springframework.data.redis.core.RedisCallback<byte[]>) conn -> conn.stringCommands()
                .get(key.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    /** Redis 位图 MSB-first：字节最高位是 8n 号位（实测 SETBIT 0 → 0x80） */
    private static boolean bitAt(byte[] bytes, int off) {
        int idx = off / 8;
        if (idx >= bytes.length) {
            return false;
        }
        return ((bytes[idx] >> (7 - (off % 8))) & 1) == 1;
    }

    private static String lockKey(Long screeningId) {
        return String.format(LOCK_KEY, screeningId);
    }

    private static String soldKey(Long screeningId) {
        return String.format(SOLD_KEY, screeningId);
    }

    /** ARGV: [seatCols, row1, col1, row2, col2, ...]（Object[] 才能被 varargs 展开） */
    private static Object[] buildArgs(List<int[]> seats, int seatCols) {
        Object[] args = new Object[seats.size() * 2 + 1];
        args[0] = String.valueOf(seatCols);
        int i = 1;
        for (int[] s : seats) {
            args[i++] = String.valueOf(s[0]);
            args[i++] = String.valueOf(s[1]);
        }
        return args;
    }

    /** 第一遍检查（sold/lock 位任一为 1 即冲突），全部通过第二遍置 lock 位；原子。返回冲突偏移数组 */
    private static final DefaultRedisScript<List> LOCK_SCRIPT = new DefaultRedisScript<>("""
            local cols = tonumber(ARGV[1])
            local n = (#ARGV - 1) / 2
            local conflicts = {}
            for i = 1, n do
              local off = (tonumber(ARGV[2 + (i-1)*2]) - 1) * cols + (tonumber(ARGV[3 + (i-1)*2]) - 1)
              if redis.call('GETBIT', KEYS[1], off) == 1 or redis.call('GETBIT', KEYS[2], off) == 1 then
                conflicts[#conflicts + 1] = off
              end
            end
            if #conflicts > 0 then return conflicts end
            for i = 1, n do
              local off = (tonumber(ARGV[2 + (i-1)*2]) - 1) * cols + (tonumber(ARGV[3 + (i-1)*2]) - 1)
              redis.call('SETBIT', KEYS[1], off, 1)
            end
            return {}
            """, List.class);

    /** 清 lock 位 */
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>("""
            local cols = tonumber(ARGV[1])
            local n = (#ARGV - 1) / 2
            for i = 1, n do
              local off = (tonumber(ARGV[2 + (i-1)*2]) - 1) * cols + (tonumber(ARGV[3 + (i-1)*2]) - 1)
              redis.call('SETBIT', KEYS[1], off, 0)
            end
            return 1
            """, Long.class);

    /** 置 sold 位 + 清 lock 位 */
    private static final DefaultRedisScript<Long> MARK_SOLD_SCRIPT = new DefaultRedisScript<>("""
            local cols = tonumber(ARGV[1])
            local n = (#ARGV - 1) / 2
            for i = 1, n do
              local off = (tonumber(ARGV[2 + (i-1)*2]) - 1) * cols + (tonumber(ARGV[3 + (i-1)*2]) - 1)
              redis.call('SETBIT', KEYS[2], off, 1)
              redis.call('SETBIT', KEYS[1], off, 0)
            end
            return 1
            """, Long.class);
}

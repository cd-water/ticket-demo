package com.cdwater.cdticket.app.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * 超时关单延迟队列：Redis ZSet（member=orderId, score=支付截止时间戳秒）。
 * 轮询取到期订单，替代 MQ 延迟队列，避免无效消息积压。
 */
@Service
@RequiredArgsConstructor
public class OrderDelayQueueService {

    private static final String DELAY_KEY = "order:delay";

    private final StringRedisTemplate redis;

    public void push(Long orderId, LocalDateTime expireTime) {
        long score = expireTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        redis.opsForZSet().add(DELAY_KEY, String.valueOf(orderId), score);
    }

    /** 已到期订单（最多 100 个，按到期时间先后） */
    public List<Long> pollDue() {
        Set<String> members = redis.opsForZSet().rangeByScore(DELAY_KEY, 0,
                System.currentTimeMillis() / 1000, 0, 100);
        return members == null ? List.of() : members.stream().map(Long::valueOf).toList();
    }

    public void remove(Long orderId) {
        redis.opsForZSet().remove(DELAY_KEY, String.valueOf(orderId));
    }
}

package com.cdwater.cdticket.app.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 滑动窗口限流：Redis ZSet 记录窗口内每次请求时间戳，Lua 原子执行。
 * key 形如 rl:orders:user:{userId} / rl:seats:screening:{screeningId}
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redis;

    /** 窗口 windowSec 内最多 max 次，超限返回 false */
    public boolean allow(String key, long windowSec, long max) {
        Long result = redis.execute(SCRIPT, List.of(key),
                String.valueOf(System.currentTimeMillis()), String.valueOf(windowSec), String.valueOf(max));
        return result != null && result == 1L;
    }

    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>("""
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2]) * 1000
            local max = tonumber(ARGV[3])
            redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, now - window)
            if redis.call('ZCARD', KEYS[1]) < max then
              redis.call('ZADD', KEYS[1], now, now .. ':' .. math.random(1, 999999))
              redis.call('EXPIRE', KEYS[1], window / 1000 * 2)
              return 1
            end
            return 0
            """, Long.class);
}

package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.common.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenStoreService {
    private static final String KEY_PREFIX = "admin:token:";

    private final StringRedisTemplate redis;
    private final JwtProperties jwtProperties;

    /** 覆盖写实现单设备踢线：旧 token 立即失效。 */
    public void store(Long adminId, String token) {
        redis.opsForValue().set(KEY_PREFIX + adminId, token,
                Duration.ofSeconds(jwtProperties.getAccessExpireSeconds()));
    }

    public void remove(Long adminId) {
        redis.delete(KEY_PREFIX + adminId);
    }
}

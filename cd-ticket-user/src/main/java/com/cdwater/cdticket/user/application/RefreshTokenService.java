package com.cdwater.cdticket.user.application;

import com.cdwater.cdticket.common.application.BizException;
import com.cdwater.cdticket.common.application.ResultCode;
import com.cdwater.cdticket.common.infrastructure.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final String KEY_PREFIX = "refresh:token:";

    private final StringRedisTemplate redis;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(StringRedisTemplate redis, JwtProperties jwtProperties) {
        this.redis = redis;
        this.jwtProperties = jwtProperties;
    }

    public String create(Long userId) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(KEY_PREFIX + token, String.valueOf(userId),
                Duration.ofSeconds(jwtProperties.getRefreshExpireSeconds()));
        return token;
    }

    /** 返回 token 对应的 userId；token 无效抛 1002。 */
    public Long getUserId(String token) {
        String userId = redis.opsForValue().get(KEY_PREFIX + token);
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return Long.valueOf(userId);
    }

    /** 轮换：校验 token 有效 → 删除旧 token → 返回新 token。 */
    public String rotate(String token) {
        Long userId = getUserId(token);
        redis.delete(KEY_PREFIX + token);
        return create(userId);
    }

    public void revoke(String token) {
        redis.delete(KEY_PREFIX + token);
    }
}

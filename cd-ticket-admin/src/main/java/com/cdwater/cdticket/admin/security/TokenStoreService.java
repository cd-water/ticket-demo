package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.config.TokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 管理端会话存储：token 是不带签名的 UUID，状态全部在 Redis。
 * 认证时按 token 反查 adminId；同一管理员只保留最新 token（单设备踢线）。
 */
@Service
@RequiredArgsConstructor
public class TokenStoreService {

    /** token → adminId（认证查找方向） */
    private static final String TOKEN_KEY = "admin:token:";
    /** adminId → token（单设备踢线：只保留最新 token） */
    private static final String CURRENT_KEY = "admin:current:";

    private final StringRedisTemplate redis;
    private final TokenProperties props;

    /** 签发新 token；同一管理员的旧 token 立即失效 */
    public String issue(Long adminId) {
        String previous = redis.opsForValue().get(CURRENT_KEY + adminId);
        if (previous != null) {
            redis.delete(TOKEN_KEY + previous);
        }
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(TOKEN_KEY + token, String.valueOf(adminId),
                Duration.ofSeconds(props.getExpireSeconds()));
        redis.opsForValue().set(CURRENT_KEY + adminId, token,
                Duration.ofSeconds(props.getExpireSeconds()));
        return token;
    }

    /** 查 token 对应的管理员；无效/过期返回 null */
    public Long resolveAdminId(String token) {
        String value = redis.opsForValue().get(TOKEN_KEY + token);
        return value == null ? null : Long.valueOf(value);
    }

    /** 滑动续期：两个 key 的 TTL 一起重置 */
    public void renew(String token, Long adminId) {
        redis.expire(TOKEN_KEY + token, Duration.ofSeconds(props.getExpireSeconds()));
        redis.expire(CURRENT_KEY + adminId, Duration.ofSeconds(props.getExpireSeconds()));
    }

    /** 注销：作废该管理员当前 token */
    public void revoke(Long adminId) {
        String token = redis.opsForValue().get(CURRENT_KEY + adminId);
        if (token != null) {
            redis.delete(TOKEN_KEY + token);
        }
        redis.delete(CURRENT_KEY + adminId);
    }
}

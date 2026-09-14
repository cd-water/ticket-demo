package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.config.TokenProperties;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenStoreService {
    private static final String TOKEN_KEY = "admin:token:";
    private static final String CURRENT_KEY = "admin:current:";

    private final StringRedisTemplate redis;
    private final AdminMapper adminMapper;
    private final TokenProperties props;

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

    public TokenAuthenticationFilter.AdminContext resolve(String token) {
        String idStr = redis.opsForValue().get(TOKEN_KEY + token);
        if (idStr == null) return null;
        Admin admin = adminMapper.selectById(Long.valueOf(idStr));
        if (admin == null || admin.getStatus() == null || admin.getStatus() != 1) return null;
        return new TokenAuthenticationFilter.AdminContext(
                admin.getId(), admin.getRole(), admin.getCinemaId());
    }

    /** 两个 key 必须同寿命续期，否则 CURRENT_KEY 先过期后 revoke 找不到 token，登出/踢线失效 */
    public void renew(String token, Long adminId) {
        Duration ttl = Duration.ofSeconds(props.getExpireSeconds());
        redis.expire(TOKEN_KEY + token, ttl);
        redis.expire(CURRENT_KEY + adminId, ttl);
    }

    public void revoke(Long adminId) {
        String token = redis.opsForValue().get(CURRENT_KEY + adminId);
        if (token != null) {
            redis.delete(TOKEN_KEY + token);
        }
        redis.delete(CURRENT_KEY + adminId);
    }
}

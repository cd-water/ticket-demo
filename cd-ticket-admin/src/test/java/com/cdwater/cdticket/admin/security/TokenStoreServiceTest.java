package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.config.TokenProperties;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenStoreServiceTest {
    private static final long ADMIN_ID = 7L;

    private StringRedisTemplate redis;
    private ValueOperations<String, String> values;
    private TokenStoreService tokenStore;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        TokenProperties props = new TokenProperties();
        props.setExpireSeconds(86400L);
        tokenStore = new TokenStoreService(redis, mock(AdminMapper.class), props);
    }

    /** 两个 key 必须同寿命续期，否则 CURRENT_KEY 先过期，revoke 就找不到 token */
    @Test
    void renewKeepsBothKeysAlive() {
        tokenStore.renew("t1", ADMIN_ID);
        verify(redis).expire(eq("admin:token:t1"), any(Duration.class));
        verify(redis).expire(eq("admin:current:7"), any(Duration.class));
    }

    /** 单设备登录：同一管理员再次登录必须踢掉上一个 token */
    @Test
    void issueRevokesPreviousToken() {
        when(values.get("admin:current:7")).thenReturn("old-token");
        String token = tokenStore.issue(ADMIN_ID);
        verify(redis).delete("admin:token:old-token");
        verify(values).set(eq("admin:token:" + token), eq("7"), any(Duration.class));
        verify(values).set(eq("admin:current:7"), eq(token), any(Duration.class));
    }

    /** 登出：两个 key 都要删，否则 token 仍可继续访问 */
    @Test
    void revokeDropsBothKeys() {
        when(values.get("admin:current:7")).thenReturn("t1");
        tokenStore.revoke(ADMIN_ID);
        verify(redis).delete("admin:token:t1");
        verify(redis).delete("admin:current:7");
    }
}

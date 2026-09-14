package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.config.TokenProperties;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenStoreServiceTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private AdminMapper adminMapper;
    @Mock
    private ValueOperations<String, String> valueOps;

    private TokenStoreService tokenStore;

    @BeforeEach
    void setUp() {
        TokenProperties props = new TokenProperties();
        props.setExpireSeconds(3600L);
        tokenStore = new TokenStoreService(redis, adminMapper, props);
    }

    private void stubValueOps() {
        when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void issue_withoutPreviousToken_setsBothKeys() {
        stubValueOps();
        when(valueOps.get("admin:current:1")).thenReturn(null);

        String token = tokenStore.issue(1L);

        assertThat(token).isNotBlank();
        verify(valueOps).set("admin:token:" + token, "1", Duration.ofSeconds(3600));
        verify(valueOps).set("admin:current:1", token, Duration.ofSeconds(3600));
        verify(redis, never()).delete(anyString());
    }

    @Test
    void issue_withPreviousToken_deletesOldToken() {
        stubValueOps();
        when(valueOps.get("admin:current:1")).thenReturn("old-token");

        tokenStore.issue(1L);

        verify(redis).delete("admin:token:old-token");
    }

    @Test
    void resolve_tokenMissing_returnsNull() {
        stubValueOps();
        when(valueOps.get("admin:token:t1")).thenReturn(null);
        assertThat(tokenStore.resolve("t1")).isNull();
    }

    @Test
    void resolve_activeAdmin_returnsId() {
        stubValueOps();
        when(valueOps.get("admin:token:t1")).thenReturn("5");
        Admin admin = new Admin();
        admin.setId(5L);
        admin.setStatus(1);
        when(adminMapper.selectById(5L)).thenReturn(admin);

        assertThat(tokenStore.resolve("t1")).isEqualTo(5L);
    }

    @Test
    void resolve_adminMissing_returnsNull() {
        stubValueOps();
        when(valueOps.get("admin:token:t1")).thenReturn("5");
        when(adminMapper.selectById(5L)).thenReturn(null);

        assertThat(tokenStore.resolve("t1")).isNull();
    }

    @Test
    void resolve_disabledAdmin_returnsNull() {
        stubValueOps();
        when(valueOps.get("admin:token:t1")).thenReturn("5");
        Admin admin = new Admin();
        admin.setId(5L);
        admin.setStatus(0);
        when(adminMapper.selectById(5L)).thenReturn(admin);

        assertThat(tokenStore.resolve("t1")).isNull();
    }

    @Test
    void renew_refreshesBothKeys() {
        tokenStore.renew("t1", 5L);
        verify(redis).expire("admin:token:t1", Duration.ofSeconds(3600));
        verify(redis).expire("admin:current:5", Duration.ofSeconds(3600));
    }

    @Test
    void revoke_withCurrentToken_deletesBoth() {
        stubValueOps();
        when(valueOps.get("admin:current:7")).thenReturn("tok7");

        tokenStore.revoke(7L);

        verify(redis).delete("admin:token:tok7");
        verify(redis).delete("admin:current:7");
    }

    @Test
    void revoke_withoutCurrentToken_stillDeletesCurrentKey() {
        stubValueOps();
        when(valueOps.get("admin:current:7")).thenReturn(null);

        tokenStore.revoke(7L);

        verify(redis).delete("admin:current:7");
        verify(redis, never()).delete("admin:token:tok7");
    }
}

package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.config.TokenProperties;
import com.cdwater.cdticket.admin.dto.admin.AdminLoginResponse;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.TokenStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAuthServiceTest {
    private AdminMapper adminMapper;
    private StringRedisTemplate redis;
    private AdminAuthService service;

    @BeforeEach
    void setUp() {
        adminMapper = mock(AdminMapper.class);
        redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        TokenProperties props = new TokenProperties();
        props.setExpireSeconds(86400L);
        TokenStoreService tokenStore = new TokenStoreService(redis, props);
        service = new AdminAuthService(adminMapper, tokenStore, new BCryptPasswordEncoder());
    }

    @Test
    void loginStoresTokenInRedis() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode("Aa123456"));
        admin.setRole(0);
        admin.setCinemaId(0L);
        admin.setStatus(1);
        when(adminMapper.selectOne(any())).thenReturn(admin);
        AdminLoginResponse resp = service.login("admin", "Aa123456");

        // token 是 UUID（不再带签名/分段），且以 token→adminId、adminId→token 两个 key 落 Redis
        assertDoesNotThrow(() -> UUID.fromString(resp.getToken()));
        verify(redis.opsForValue()).set(eq("admin:token:" + resp.getToken()), eq("1"),
                eq(Duration.ofSeconds(86400)));
        verify(redis.opsForValue()).set(eq("admin:current:1"), eq(resp.getToken()),
                eq(Duration.ofSeconds(86400)));
    }

    @Test
    void loginRejectsDisabledAdmin() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setPassword(new BCryptPasswordEncoder().encode("Aa123456"));
        admin.setStatus(0);
        when(adminMapper.selectOne(any())).thenReturn(admin);
        assertThrows(BizException.class, () -> service.login("admin", "Aa123456"));
    }

    @Test
    void loginRejectsWrongPassword() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setPassword(new BCryptPasswordEncoder().encode("right"));
        admin.setStatus(1);
        when(adminMapper.selectOne(any())).thenReturn(admin);
        assertThrows(BizException.class, () -> service.login("admin", "wrong"));
    }
}

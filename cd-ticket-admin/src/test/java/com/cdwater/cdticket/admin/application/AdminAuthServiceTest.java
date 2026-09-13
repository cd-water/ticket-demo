package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.application.dto.AdminLoginResponse;
import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.domain.entity.Admin;
import com.cdwater.cdticket.admin.common.BizException;
import com.cdwater.cdticket.admin.common.JwtProperties;
import com.cdwater.cdticket.admin.common.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAuthServiceTest {
    private AdminRepository adminRepository;
    private StringRedisTemplate redis;
    private AdminAuthService service;

    @BeforeEach
    void setUp() {
        adminRepository = mock(AdminRepository.class);
        redis = mock(StringRedisTemplate.class);
        when(redis.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        JwtProperties props = new JwtProperties();
        props.setSecret("cd-ticket-dev-secret-key-0123456789abcdef0123456789abcdef");
        props.setAccessExpireSeconds(900L);
        props.setRefreshExpireSeconds(604800L);
        TokenStoreService tokenStore = new TokenStoreService(redis, props);
        service = new AdminAuthService(adminRepository, tokenStore, new JwtUtil(props), new BCryptPasswordEncoder());
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
        when(adminRepository.findByUsername("admin")).thenReturn(admin);
        AdminLoginResponse resp = service.login("admin", "Aa123456");
        assertNotNull(resp.getToken());
        verify(redis).opsForValue();
    }

    @Test
    void loginRejectsDisabledAdmin() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setPassword(new BCryptPasswordEncoder().encode("Aa123456"));
        admin.setStatus(0);
        when(adminRepository.findByUsername("admin")).thenReturn(admin);
        assertThrows(BizException.class, () -> service.login("admin", "Aa123456"));
    }

    @Test
    void loginRejectsWrongPassword() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setPassword(new BCryptPasswordEncoder().encode("right"));
        admin.setStatus(1);
        when(adminRepository.findByUsername("admin")).thenReturn(admin);
        assertThrows(BizException.class, () -> service.login("admin", "wrong"));
    }
}
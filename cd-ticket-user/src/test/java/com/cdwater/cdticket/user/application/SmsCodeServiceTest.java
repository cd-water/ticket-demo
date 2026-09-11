package com.cdwater.cdticket.user.application;

import com.cdwater.cdticket.common.application.BizException;
import com.cdwater.cdticket.user.infrastructure.SmsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmsCodeServiceTest {
    private StringRedisTemplate redis;
    private ValueOperations<String, String> ops;
    private SmsCodeService service;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        SmsProperties props = new SmsProperties();
        props.setCodeExpireSeconds(300);
        props.setSendCooldownSeconds(60);
        SmsSender sender = mock(SmsSender.class);
        service = new SmsCodeService(redis, props, sender);
    }

    @Test
    void sendCodeStoresCodeAndSetsCooldown() {
        service.sendCode("13800138000");
        verify(ops).set(eq("sms:13800138000"), anyString(), eq(Duration.ofSeconds(300)));
        verify(ops).set(eq("sms:send:13800138000"), eq("1"), eq(Duration.ofSeconds(60)));
    }

    @Test
    void sendCodeRejectsInvalidPhone() {
        assertThrows(BizException.class, () -> service.sendCode("123"));
    }

    @Test
    void sendCodeRejectsWhenCooldown() {
        when(redis.hasKey("sms:send:13800138000")).thenReturn(true);
        assertThrows(BizException.class, () -> service.sendCode("13800138000"));
    }

    @Test
    void verifyPassesAndDeletesCode() {
        when(ops.get("sms:13800138000")).thenReturn("123456");
        service.verify("13800138000", "123456");
        verify(redis).delete("sms:13800138000");
    }

    @Test
    void verifyRejectsWrongCode() {
        when(ops.get("sms:13800138000")).thenReturn("123456");
        assertThrows(BizException.class, () -> service.verify("13800138000", "999999"));
    }
}

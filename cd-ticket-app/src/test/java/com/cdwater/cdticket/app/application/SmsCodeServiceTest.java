package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.SmsCodeService;
import com.cdwater.cdticket.app.application.SmsSender;
import com.cdwater.cdticket.app.common.exception.BizException;
import com.cdwater.cdticket.app.infrastructure.config.SmsProperties;
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
    private SmsSender sender;
    private SmsCodeService service;

    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        SmsProperties props = new SmsProperties();
        props.setCodeExpireSeconds(300L);
        sender = mock(SmsSender.class);
        service = new SmsCodeService(redis, props, sender);
    }

    @Test
    void sendCodeStoresCodeWithTtl() {
        service.sendCode("13800138000");
        verify(ops).set(eq("sms:13800138000"), anyString(), eq(Duration.ofSeconds(300)));
        verify(sender).send(eq("13800138000"), anyString());
    }

    @Test
    void sendCodeOverwritesPreviousCode() {
        service.sendCode("13800138000");
        service.sendCode("13800138000");
        verify(ops, times(2)).set(eq("sms:13800138000"), anyString(), eq(Duration.ofSeconds(300)));
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
        verify(redis, never()).delete(anyString());
    }
}
package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.common.exception.BizException;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.infrastructure.config.SmsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SmsCodeService {
    private static final String SMS_KEY_PREFIX = "sms:";

    private final StringRedisTemplate redis;
    private final SmsProperties smsProperties;
    private final SmsSender smsSender;

    public void sendCode(String phone) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        redis.opsForValue().set(SMS_KEY_PREFIX + phone, code,
                Duration.ofSeconds(smsProperties.getCodeExpireSeconds()));
        smsSender.send(phone, code);
    }

    public void verify(String phone, String code) {
        String key = SMS_KEY_PREFIX + phone;
        String saved = redis.opsForValue().get(key);
        if (saved == null || !saved.equals(code)) {
            throw new BizException(ResultCode.SMS_CODE_INVALID);
        }
        redis.delete(key);
    }
}
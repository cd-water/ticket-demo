package com.cdwater.cdticket.user.application;

import com.cdwater.cdticket.common.application.BizException;
import com.cdwater.cdticket.common.application.ResultCode;
import com.cdwater.cdticket.user.infrastructure.SmsProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Service
public class SmsCodeService {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final String CODE_KEY = "sms:";
    private static final String COOLDOWN_KEY = "sms:send:";

    private final StringRedisTemplate redis;
    private final SmsProperties smsProperties;
    private final SmsSender smsSender;

    public SmsCodeService(StringRedisTemplate redis, SmsProperties smsProperties, SmsSender smsSender) {
        this.redis = redis;
        this.smsProperties = smsProperties;
        this.smsSender = smsSender;
    }

    public void sendCode(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BizException(ResultCode.PHONE_INVALID);
        }
        String cooldownKey = COOLDOWN_KEY + phone;
        if (Boolean.TRUE.equals(redis.hasKey(cooldownKey))) {
            throw new BizException(ResultCode.SMS_SEND_TOO_FREQUENT);
        }
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        redis.opsForValue().set(CODE_KEY + phone, code, Duration.ofSeconds(smsProperties.getCodeExpireSeconds()));
        redis.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(smsProperties.getSendCooldownSeconds()));
        smsSender.send(phone, code);
    }

    public void verify(String phone, String code) {
        String saved = redis.opsForValue().get(CODE_KEY + phone);
        if (saved == null || !saved.equals(code)) {
            throw new BizException(ResultCode.SMS_CODE_INVALID);
        }
        redis.delete(CODE_KEY + phone);
    }
}

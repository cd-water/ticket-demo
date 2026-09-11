package com.cdwater.cdticket.user.application;

import com.cdwater.cdticket.common.exception.BizException;
import com.cdwater.cdticket.common.api.ResultCode;
import com.cdwater.cdticket.user.infrastructure.SmsProperties;
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
        // 手机号格式校验已在 controller 的 @Valid(SmsCodeRequest) 完成
        // 短信发送不限流；如需限流，统一在更高层（API网关/切面）做
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
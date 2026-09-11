package com.cdwater.cdticket.user.infrastructure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 消费 sms.send 主题，模拟真实短信发送（生产环境替换为调用短信服务 SDK）。
 * 通过 Kafka 异步解耦验证码业务与短信下发，提升接口响应速度。
 */
@Slf4j
@Component
public class SmsConsumer {

    @KafkaListener(topics = "sms.send", groupId = "sms-consumer")
    public void onSmsMessage(SmsMessage message) {
        log.info("[SMS-SENT] phone={} code={}", message.getPhone(), message.getCode());
    }
}
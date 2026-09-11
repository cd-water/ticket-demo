package com.cdwater.cdticket.user.infrastructure.kafka;

import com.cdwater.cdticket.user.application.SmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 通过 Kafka 异步发送短信（生产者端）。
 * 实际下发由下游 Kafka 消费者完成，便于接入真实短信服务时无需改动本服务。
 */
@Component
@RequiredArgsConstructor
public class KafkaSmsSender implements SmsSender {

    private static final String TOPIC = "sms.send";

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Override
    public void send(String phone, String code) {
        SmsMessage message = new SmsMessage();
        message.setPhone(phone);
        message.setCode(code);
        kafkaTemplate.send(TOPIC, phone, message);
    }
}
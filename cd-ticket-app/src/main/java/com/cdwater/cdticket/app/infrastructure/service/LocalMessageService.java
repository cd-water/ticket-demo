package com.cdwater.cdticket.app.infrastructure.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.app.domain.model.LocalMessage;
import com.cdwater.cdticket.app.infrastructure.mapper.LocalMessageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表：业务事务内落表，定时任务扫描投递 Kafka，保证消息可靠性（失败重试，上限终态失败）。
 * msgType: SMS 短信验证码 / BOX_OFFICE 票房统计
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalMessageService {

    public static final String TYPE_SMS = "SMS";
    public static final String TYPE_BOX_OFFICE = "BOX_OFFICE";
    public static final String TOPIC_SMS = "sms.send";
    public static final String TOPIC_BOX_OFFICE = "box.office";
    private static final int MAX_RETRY = 5;
    private static final int BATCH_SIZE = 100;

    private final LocalMessageMapper localMessageMapper;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /** 业务事务内调用（与业务写操作同事务，失败随业务回滚） */
    public void create(String msgType, Long bizId, Object payload) {
        LocalMessage msg = new LocalMessage();
        msg.setMsgType(msgType);
        msg.setBizId(bizId);
        msg.setPayload(toJson(payload));
        msg.setStatus(0);
        msg.setRetryCount(0);
        msg.setNextRetryTime(LocalDateTime.now());
        localMessageMapper.insert(msg);
    }

    /** 定时扫描待发送消息投递 Kafka；失败按 2^retry 指数退避重试，超上限置终态失败 */
    @Scheduled(fixedDelay = 10_000)
    public void dispatch() {
        List<LocalMessage> pending = localMessageMapper.selectList(new LambdaQueryWrapper<LocalMessage>()
                .eq(LocalMessage::getStatus, 0)
                .le(LocalMessage::getNextRetryTime, LocalDateTime.now())
                .orderByAsc(LocalMessage::getId)
                .last("LIMIT " + BATCH_SIZE));
        for (LocalMessage msg : pending) {
            try {
                // 以 JsonNode 发送，Kafka JsonSerializer 序列化为 JSON 对象，下游可反序列化为具体消息类
                var payload = objectMapper.readTree(msg.getPayload());
                switch (msg.getMsgType()) {
                    case TYPE_SMS -> kafkaTemplate.send(TOPIC_SMS, msg.getBizId(), payload);
                    case TYPE_BOX_OFFICE -> kafkaTemplate.send(TOPIC_BOX_OFFICE, msg.getBizId(), payload);
                    default -> throw new IllegalArgumentException("unknown msgType: " + msg.getMsgType());
                }
                msg.setStatus(1);
                localMessageMapper.updateById(msg);
            } catch (Exception e) {
                log.error("dispatch local message failed, id={}", msg.getId(), e);
                msg.setRetryCount(msg.getRetryCount() + 1);
                if (msg.getRetryCount() >= MAX_RETRY) {
                    msg.setStatus(2); // 终态失败
                } else {
                    msg.setNextRetryTime(LocalDateTime.now().plusMinutes(1L << Math.min(msg.getRetryCount(), 5)));
                }
                localMessageMapper.updateById(msg);
            }
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("local message serialize failed", e);
        }
    }
}

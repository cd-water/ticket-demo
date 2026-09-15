package com.cdwater.cdticket.app.infrastructure.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * 消费 box.office 主题，异步累计今日票房到 Redis ZSet（member=movieId, score=金额）。
 * ZSet 每日 00:05 过期（EXPIREAT 固定时间点，不随刷新顺延）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoxOfficeConsumer {

    private static final String BOX_KEY = "box:office:today";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "box.office", groupId = "box-office-consumer")
    public void onBoxOffice(JsonNode message) {
        try {
            Long movieId = message.path("movieId").asLong();
            double amount = message.path("amount").asDouble();
            redis.opsForZSet().incrementScore(BOX_KEY, String.valueOf(movieId), amount);
            // 过期时间固定为次日 00:05，重复消息不会顺延 TTL
            long expireAt = LocalDate.now().plusDays(1).atStartOfDay().plusMinutes(5)
                    .atZone(ZoneId.systemDefault()).toEpochSecond();
            redis.expireAt(BOX_KEY, java.time.Instant.ofEpochSecond(expireAt));
        } catch (Exception e) {
            log.error("box office message handle failed", e);
        }
    }
}

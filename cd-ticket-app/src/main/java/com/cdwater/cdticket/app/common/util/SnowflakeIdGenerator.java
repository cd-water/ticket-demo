package com.cdwater.cdticket.app.common.util;

import org.springframework.stereotype.Component;

/**
 * 雪花 ID 生成器（订单号/支付流水号/退款流水号）。
 * 单实例部署 workerId 固定 1；多实例需按实例配置 workerId。
 */
@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1288834974657L; // Twitter 雪花纪元（2010-11-04）
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    private final long workerId = 1L;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            // 时钟回拨：等待追上最后时间戳
            timestamp = waitUntil(lastTimestamp);
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitUntil(lastTimestamp + 1);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitUntil(long target) {
        long t = System.currentTimeMillis();
        while (t < target) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            t = System.currentTimeMillis();
        }
        return t;
    }
}

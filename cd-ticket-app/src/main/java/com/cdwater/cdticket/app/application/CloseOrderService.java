package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.domain.model.Order;
import com.cdwater.cdticket.app.domain.repository.OrderRepository;
import com.cdwater.cdticket.app.infrastructure.service.OrderDelayQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 超时关单：轮询 Redis ZSet 延迟队列，到期订单 CAS 置已取消（与支付并发以 status 乐观锁决胜负），
 * 成功后释放座位锁位。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloseOrderService {

    private final OrderDelayQueueService delayQueueService;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 5_000)
    public void closeExpired() {
        for (Long orderId : delayQueueService.pollDue()) {
            try {
                Order order = orderRepository.findById(orderId);
                delayQueueService.remove(orderId);
                if (order == null) {
                    continue;
                }
                if (orderRepository.cancel(orderId, OrderService.CANCEL_REASON_EXPIRED)) {
                    orderService.releaseSeats(order);
                }
                // cancel 失败 = 已被支付/取消，无需处理
            } catch (Exception e) {
                log.error("close order failed, orderId={}", orderId, e);
            }
        }
    }
}

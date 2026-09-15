package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.OrderVO;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.common.exception.BizException;
import com.cdwater.cdticket.app.domain.model.Hall;
import com.cdwater.cdticket.app.domain.model.Order;
import com.cdwater.cdticket.app.domain.model.OrderItem;
import com.cdwater.cdticket.app.domain.model.Screening;
import com.cdwater.cdticket.app.domain.repository.OrderRepository;
import com.cdwater.cdticket.app.domain.repository.ScreeningRepository;
import com.cdwater.cdticket.app.infrastructure.service.LocalMessageService;
import com.cdwater.cdticket.app.infrastructure.service.OrderDelayQueueService;
import com.cdwater.cdticket.app.infrastructure.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟支付：无第三方网关，接口同步完成 0→1。
 * 支付与关单/取消并发以 status CAS（乐观锁）决出胜负；失败方走退款补偿：
 * 模拟网关已扣款 → 落支付成功流水 + 自动退款流水（对应 project-tech 的"支付回调发现订单已取消自动退款"）。
 */
@Service
@RequiredArgsConstructor
public class PayService {

    private final OrderRepository orderRepository;
    private final ScreeningRepository screeningRepository;
    private final OrderDelayQueueService delayQueueService;
    private final SeatLockService seatLockService;
    private final OrderService orderService;
    private final LocalMessageService localMessageService;
    private final CompensationService compensationService;

    public OrderVO pay(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        // 已支付：幂等拒绝，不重复扣款、不重新生成取票码
        if (order.getStatus() == 1) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID);
        }
        // 已取消（含超时已关单）：模拟网关扣款成功 → 自动退款补偿
        if (order.getStatus() == 2) {
            compensationService.compensateRefund(order, "订单已取消，自动退款");
            throw new BizException(ResultCode.ORDER_EXPIRED);
        }
        // 待支付但已超时：先关单，再走退款补偿
        if (order.getPayExpireTime().isBefore(LocalDateTime.now())) {
            orderRepository.cancel(order.getId(), OrderService.CANCEL_REASON_EXPIRED);
            orderService.releaseSeats(order);
            delayQueueService.remove(order.getId());
            compensationService.compensateRefund(order, "订单已超时关闭，自动退款");
            throw new BizException(ResultCode.ORDER_EXPIRED);
        }

        String ticketCode = genTicketCode();
        if (!orderRepository.markPaid(orderId, ticketCode)) {
            // 与关单/取消并发落败：订单已非待支付 → 退款补偿
            compensationService.compensateRefund(order, "订单已取消，自动退款");
            throw new BizException(ResultCode.ORDER_EXPIRED);
        }

        // 支付成功：座位置已售 + 票房统计异步消息（本地消息表 → Kafka）
        List<OrderItem> items = orderRepository.itemsByOrderId(orderId);
        Screening s = screeningRepository.findById(order.getScreeningId());
        Hall hall = screeningRepository.findHall(s.getHallId());
        seatLockService.markSold(toPositions(items), order.getScreeningId(), hall.getSeatCols());
        localMessageService.create(LocalMessageService.TYPE_BOX_OFFICE, orderId,
                Map.of("movieId", order.getMovieId(), "amount", order.getTotalAmount()));
        delayQueueService.remove(orderId);
        return orderService.detail(userId, orderId);
    }

    private static List<int[]> toPositions(List<OrderItem> items) {
        return items.stream().map(i -> new int[]{i.getSeatRow(), i.getSeatCol()}).toList();
    }

    private static String genTicketCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000));
    }
}

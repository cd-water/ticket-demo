package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.common.util.SnowflakeIdGenerator;
import com.cdwater.cdticket.app.domain.model.Order;
import com.cdwater.cdticket.app.domain.model.Payment;
import com.cdwater.cdticket.app.domain.model.Refund;
import com.cdwater.cdticket.app.infrastructure.mapper.PaymentMapper;
import com.cdwater.cdticket.app.infrastructure.mapper.RefundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 退款补偿：模拟网关已扣款 → 落支付成功流水 + 自动退款流水。
 * 独立事务（REQUIRES_NEW）确保即使外层事务因抛出业务异常被回滚，补偿流水也已落地。
 */
@Service
@RequiredArgsConstructor
public class CompensationService {

    private static final int PAY_CHANNEL_ALIPAY = 1;

    private final PaymentMapper paymentMapper;
    private final RefundMapper refundMapper;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void compensateRefund(Order order, String reason) {
        Payment payment = new Payment();
        payment.setPaymentNo(idGenerator.nextId());
        payment.setOrderId(order.getId());
        payment.setChannel(PAY_CHANNEL_ALIPAY);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(1);
        payment.setOutTradeNo("MOCK" + order.getOrderNo());
        payment.setTradeNo("MOCKTRADE" + order.getOrderNo());
        payment.setPaidTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        Refund refund = new Refund();
        refund.setRefundNo(idGenerator.nextId());
        refund.setPaymentId(payment.getId());
        refund.setOrderId(order.getId());
        refund.setChannel(PAY_CHANNEL_ALIPAY);
        refund.setRefundAmount(order.getTotalAmount());
        refund.setStatus(2);
        refund.setOutRefundNo("MOCKREFUND" + order.getOrderNo());
        refund.setReason(reason);
        refund.setRefundTime(LocalDateTime.now());
        refundMapper.insert(refund);
    }
}
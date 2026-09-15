package com.cdwater.cdticket.app.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdwater.cdticket.app.application.dto.OrderRowVO;
import com.cdwater.cdticket.app.application.dto.SeatPos;
import com.cdwater.cdticket.app.domain.model.Order;
import com.cdwater.cdticket.app.domain.model.OrderItem;
import com.cdwater.cdticket.app.domain.repository.OrderRepository;
import com.cdwater.cdticket.app.infrastructure.mapper.OrderItemMapper;
import com.cdwater.cdticket.app.infrastructure.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            orderMapper.insert(order);
        } else {
            orderMapper.updateById(order);
        }
        return order;
    }

    @Override
    public void insertItems(List<OrderItem> items) {
        items.forEach(orderItemMapper::insert);
    }

    @Override
    public Order findById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public IPage<OrderRowVO> pageByUser(IPage<?> page, Long userId, Integer status) {
        return orderMapper.selectPageByUser(page, userId, status);
    }

    @Override
    public OrderRowVO rowById(Long id) {
        return orderMapper.selectRowById(id);
    }

    @Override
    public List<OrderItem> itemsByOrderId(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
    }

    @Override
    public List<OrderItem> itemsByOrderIds(Collection<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return List.of();
        }
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds));
    }

    @Override
    public List<SeatPos> soldSeatsByScreening(Long screeningId) {
        return orderMapper.selectSoldSeatsByScreening(screeningId);
    }

    @Override
    public boolean cancel(Long id, String reason) {
        return orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, id)
                .eq(Order::getStatus, 0)
                .set(Order::getStatus, 2)
                .set(Order::getCancelReason, reason)) > 0;
    }

    @Override
    public boolean markPaid(Long id, String ticketCode) {
        return orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, id)
                .eq(Order::getStatus, 0)
                .set(Order::getStatus, 1)
                .set(Order::getPayTime, LocalDateTime.now())
                .set(Order::getTicketCode, ticketCode)) > 0;
    }
}

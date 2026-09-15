package com.cdwater.cdticket.app.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdwater.cdticket.app.application.dto.OrderRowVO;
import com.cdwater.cdticket.app.application.dto.SeatPos;
import com.cdwater.cdticket.app.domain.model.Order;
import com.cdwater.cdticket.app.domain.model.OrderItem;

import java.util.Collection;
import java.util.List;

public interface OrderRepository {

    Order save(Order order);

    void insertItems(List<OrderItem> items);

    Order findById(Long id);

    /** 我的订单分页（JOIN 电影/影院/影厅/排场） */
    IPage<OrderRowVO> pageByUser(IPage<?> page, Long userId, Integer status);

    /** 订单 JOIN 投影（详情/支付响应） */
    OrderRowVO rowById(Long id);

    List<OrderItem> itemsByOrderId(Long orderId);

    List<OrderItem> itemsByOrderIds(Collection<Long> orderIds);

    /** 某排场已支付订单占用的座位（用于初始化已售位图） */
    List<SeatPos> soldSeatsByScreening(Long screeningId);

    /** 取消：status 0→2（CAS，仅待支付可成功），返回是否成功 */
    boolean cancel(Long id, String reason);

    /** 支付成功：status 0→1（CAS），写支付时间与取票码 */
    boolean markPaid(Long id, String ticketCode);
}

package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.dto.order.OrderItemVO;
import com.cdwater.cdticket.admin.dto.order.OrderVO;
import com.cdwater.cdticket.admin.entity.OrderItem;
import com.cdwater.cdticket.admin.mapper.OrderItemMapper;
import com.cdwater.cdticket.admin.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public PageResult<OrderVO> pageByCinema(int page, int size, Long orderNo, Integer status, Long cinemaId) {
        IPage<OrderVO> p = orderMapper.selectPage(Page.of(page, size), orderNo, status, cinemaId);
        return PageResult.of(p);
    }

    public List<OrderItemVO> listItemsByOrder(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByAsc(OrderItem::getSeatRow, OrderItem::getSeatCol)
        );
        return items.stream().map(OrderService::toItemVO).toList();
    }

    private static OrderItemVO toItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setOrderId(item.getOrderId());
        vo.setScreeningId(item.getScreeningId());
        vo.setSeatRow(item.getSeatRow());
        vo.setSeatCol(item.getSeatCol());
        vo.setSeatNo(item.getSeatNo());
        vo.setPrice(item.getPrice());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }
}

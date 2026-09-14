package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.dto.order.OrderItemVO;
import com.cdwater.cdticket.admin.dto.order.OrderVO;
import com.cdwater.cdticket.admin.entity.OrderItem;
import com.cdwater.cdticket.admin.mapper.OrderItemMapper;
import com.cdwater.cdticket.admin.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @InjectMocks
    private OrderService orderService;

    @Test
    void pageByCinema_passesFiltersAndPaginates() {
        Page<OrderVO> page = new Page<>(2, 10);
        page.setTotal(1);
        page.setRecords(List.of(new OrderVO()));
        when(orderMapper.selectPage(any(), eq(123L), eq(1), eq(7L))).thenReturn(page);

        PageResult<OrderVO> pr = orderService.pageByCinema(2, 10, 123L, 1, 7L);

        assertThat(pr.getTotal()).isEqualTo(1);
        assertThat(pr.getPage()).isEqualTo(2);
        assertThat(pr.getSize()).isEqualTo(10);
        assertThat(pr.getRecords()).hasSize(1);
    }

    @Test
    void pageByCinema_nullFilters_allowed() {
        Page<OrderVO> page = new Page<>(1, 10);
        page.setTotal(0);
        page.setRecords(List.of());
        when(orderMapper.selectPage(any(), isNull(), isNull(), eq(7L))).thenReturn(page);

        PageResult<OrderVO> pr = orderService.pageByCinema(1, 10, null, null, 7L);

        assertThat(pr.getTotal()).isZero();
        assertThat(pr.getRecords()).isEmpty();
    }

    @Test
    void listItemsByOrder_mapsToVO() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrderId(10L);
        item.setScreeningId(100L);
        item.setSeatRow(3);
        item.setSeatCol(5);
        item.setSeatNo("3排5座");
        item.setPrice(new BigDecimal("35.00"));
        when(orderItemMapper.selectList(any())).thenReturn(List.of(item));

        List<OrderItemVO> items = orderService.listItemsByOrder(10L);

        assertThat(items).hasSize(1);
        OrderItemVO v = items.get(0);
        assertThat(v.getId()).isEqualTo(1L);
        assertThat(v.getOrderId()).isEqualTo(10L);
        assertThat(v.getScreeningId()).isEqualTo(100L);
        assertThat(v.getSeatRow()).isEqualTo(3);
        assertThat(v.getSeatCol()).isEqualTo(5);
        assertThat(v.getSeatNo()).isEqualTo("3排5座");
        assertThat(v.getPrice()).isEqualByComparingTo("35.00");
    }
}

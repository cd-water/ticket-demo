package com.cdwater.cdticket.order.application;

import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.admin.AdminPrincipal;
import com.cdwater.cdticket.order.infrastructure.mapper.OrderAdminMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderQueryServiceTest {

    private OrderAdminMapper mapper;
    private AdminAuthorizer authorizer;
    private OrderQueryService service;

    @BeforeEach
    void setUp() {
        mapper = mock(OrderAdminMapper.class);
        authorizer = mock(AdminAuthorizer.class);
        service = new OrderQueryService(mapper, authorizer);
    }

    @Test
    void cinemaAdminForcesOwnCinemaFilter() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(2L, "cinema01", 1, 5L));
        when(mapper.selectPage(any(), any(), any(), any()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));
        service.page(1, 10, "NO123", 0);
        verify(mapper).selectPage(any(), eq("NO123"), eq(0), eq(5L));
    }

    @Test
    void superAdminSeesAll() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        when(mapper.selectPage(any(), any(), any(), any()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));
        service.page(1, 10, null, null);
        verify(mapper).selectPage(any(), isNull(), isNull(), isNull());
    }
}

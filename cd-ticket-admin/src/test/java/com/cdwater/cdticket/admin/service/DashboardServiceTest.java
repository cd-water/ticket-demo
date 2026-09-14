package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.dto.dashboard.DailyOrderStat;
import com.cdwater.cdticket.admin.mapper.CinemaMapper;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import com.cdwater.cdticket.admin.mapper.OrderMapper;
import com.cdwater.cdticket.admin.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DashboardServiceTest {

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                mock(UserMapper.class), mock(MovieMapper.class), mock(CinemaMapper.class),
                mock(OrderMapper.class));
    }

    /** 近 7 日补全：从 6 天前到今天，中间缺的天补 0，保证图表横轴连续 */
    @Test
    void fillMissingDaysFillsGaps() {
        LocalDate today = LocalDate.now();
        List<DailyOrderStat> rows = List.of(
                new DailyOrderStat(today.minusDays(3).toString(), 2, new BigDecimal("40.00")),
                new DailyOrderStat(today.toString(), 1, new BigDecimal("19.90")));

        List<DailyOrderStat> filled = dashboardService.fillMissingDays(rows, 7);

        assertEquals(7, filled.size());
        assertEquals(today.minusDays(6).toString(), filled.get(0).getDate());
        assertEquals(0, filled.get(0).getOrderCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(filled.get(0).getRevenue()));
        assertEquals(2, filled.get(3).getOrderCount());
        assertEquals(new BigDecimal("40.00"), filled.get(3).getRevenue());
        assertEquals(today.toString(), filled.get(6).getDate());
        assertEquals(1, filled.get(6).getOrderCount());
    }
}

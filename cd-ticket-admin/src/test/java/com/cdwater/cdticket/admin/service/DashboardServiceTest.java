package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.dto.dashboard.CinemaStat;
import com.cdwater.cdticket.admin.dto.dashboard.DailyOrderStat;
import com.cdwater.cdticket.admin.dto.dashboard.DashboardVO;
import com.cdwater.cdticket.admin.dto.dashboard.MovieRank;
import com.cdwater.cdticket.admin.mapper.CinemaMapper;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import com.cdwater.cdticket.admin.mapper.OrderMapper;
import com.cdwater.cdticket.admin.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private MovieMapper movieMapper;
    @Mock
    private CinemaMapper cinemaMapper;
    @Mock
    private OrderMapper orderMapper;
    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void overview_aggregatesAllStats() {
        when(movieMapper.selectCount(any())).thenReturn(3L, 2L);
        when(cinemaMapper.selectCount(any())).thenReturn(4L, 1L);
        when(userMapper.selectCount(any())).thenReturn(9L);
        when(orderMapper.countByStatusSince(anyInt(), any())).thenReturn(1L, 2L, 3L);
        when(orderMapper.sumPaidSince(any())).thenReturn(new BigDecimal("100"));
        when(orderMapper.countByStatus(anyInt())).thenReturn(4L, 5L, 6L);
        when(orderMapper.sumPaidTotal()).thenReturn(new BigDecimal("1000"));

        CinemaStat stat = new CinemaStat();
        stat.setName("万达影城");
        stat.setOrderCount(2);
        stat.setRevenue(new BigDecimal("200"));
        when(cinemaMapper.selectCinemaStats()).thenReturn(List.of(stat));

        MovieRank rank = new MovieRank();
        rank.setTitle("流浪地球3");
        rank.setOrderCount(3);
        rank.setRevenue(new BigDecimal("300"));
        when(movieMapper.selectTopMovies(5)).thenReturn(List.of(rank));

        when(orderMapper.selectDailyStats(any())).thenReturn(List.of());

        DashboardVO vo = dashboardService.overview();

        assertThat(vo.getHotMovieCount()).isEqualTo(3);
        assertThat(vo.getUpcomingMovieCount()).isEqualTo(2);
        assertThat(vo.getCinemaOpenCount()).isEqualTo(4);
        assertThat(vo.getCinemaClosedCount()).isEqualTo(1);
        assertThat(vo.getUserCount()).isEqualTo(9);
        assertThat(vo.getTodayPendingCount()).isEqualTo(1);
        assertThat(vo.getTodayPaidCount()).isEqualTo(2);
        assertThat(vo.getTodayCancelledCount()).isEqualTo(3);
        assertThat(vo.getTodayRevenue()).isEqualByComparingTo("100");
        assertThat(vo.getTotalPendingCount()).isEqualTo(4);
        assertThat(vo.getTotalPaidCount()).isEqualTo(5);
        assertThat(vo.getTotalCancelledCount()).isEqualTo(6);
        assertThat(vo.getTotalRevenue()).isEqualByComparingTo("1000");
        assertThat(vo.getPerCinemaStats()).hasSize(1);
        assertThat(vo.getMovieRanking()).hasSize(1);
        assertThat(vo.getDailyStats()).hasSize(7);
    }

    @Test
    void fillMissingDays_empty_fillsSevenZeroDays() {
        List<DailyOrderStat> result = DashboardService.fillMissingDays(List.of(), 7);

        assertThat(result).hasSize(7);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.now().minusDays(6).toString());
        assertThat(result.get(6).getDate()).isEqualTo(LocalDate.now().toString());
        assertThat(result).allSatisfy(s -> {
            assertThat(s.getOrderCount()).isZero();
            assertThat(s.getRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        });
        for (int i = 1; i < result.size(); i++) {
            assertThat(LocalDate.parse(result.get(i).getDate()))
                    .isEqualTo(LocalDate.parse(result.get(i - 1).getDate()).plusDays(1));
        }
    }

    @Test
    void fillMissingDays_partialRows_keepsExistingAndFillsGaps() {
        LocalDate today = LocalDate.now();
        List<DailyOrderStat> rows = List.of(
                new DailyOrderStat(today.minusDays(3).toString(), 5, new BigDecimal("250")),
                new DailyOrderStat(today.toString(), 7, new BigDecimal("350")));

        List<DailyOrderStat> result = DashboardService.fillMissingDays(rows, 7);

        assertThat(result).hasSize(7);
        assertThat(result.get(3).getOrderCount()).isEqualTo(5);
        assertThat(result.get(3).getRevenue()).isEqualByComparingTo("250");
        assertThat(result.get(6).getOrderCount()).isEqualTo(7);
        assertThat(result.get(6).getRevenue()).isEqualByComparingTo("350");
        assertThat(result.get(0).getOrderCount()).isZero();
        assertThat(result.get(1).getOrderCount()).isZero();
    }
}

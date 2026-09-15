package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.dto.dashboard.DailyOrderStat;
import com.cdwater.cdticket.admin.dto.dashboard.DashboardVO;
import com.cdwater.cdticket.admin.entity.Cinema;
import com.cdwater.cdticket.admin.entity.Movie;
import com.cdwater.cdticket.admin.mapper.CinemaMapper;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import com.cdwater.cdticket.admin.mapper.OrderMapper;
import com.cdwater.cdticket.admin.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    /** t_order.status：0-待支付 1-已支付 2-已取消 */
    private static final int ORDER_PENDING = 0;
    private static final int ORDER_PAID = 1;
    private static final int ORDER_CANCELLED = 2;
    private static final int MOVIE_RANK_LIMIT = 5;
    private static final int DAILY_DAYS = 7;

    private final UserMapper userMapper;
    private final MovieMapper movieMapper;
    private final CinemaMapper cinemaMapper;
    private final OrderMapper orderMapper;

    public DashboardVO overview() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();

        DashboardVO vo = new DashboardVO();
        vo.setHotMovieCount(movieMapper.selectCount(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, 1).le(Movie::getReleaseDate, today)));
        vo.setUpcomingMovieCount(movieMapper.selectCount(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, 1).gt(Movie::getReleaseDate, today)));
        vo.setCinemaOpenCount(cinemaMapper.selectCount(new LambdaQueryWrapper<Cinema>().eq(Cinema::getStatus, 1)));
        vo.setCinemaClosedCount(cinemaMapper.selectCount(new LambdaQueryWrapper<Cinema>().eq(Cinema::getStatus, 0)));
        vo.setUserCount(userMapper.selectCount(null));

        long todayPending = orderMapper.countByStatusSince(ORDER_PENDING, todayStart);
        long todayPaid = orderMapper.countByStatusSince(ORDER_PAID, todayStart);
        long todayCancelled = orderMapper.countByStatusSince(ORDER_CANCELLED, todayStart);
        vo.setTodayPendingCount(todayPending);
        vo.setTodayPaidCount(todayPaid);
        vo.setTodayCancelledCount(todayCancelled);
        vo.setTodayRevenue(orderMapper.sumPaidSince(todayStart));

        long totalPending = orderMapper.countByStatus(ORDER_PENDING);
        long totalPaid = orderMapper.countByStatus(ORDER_PAID);
        long totalCancelled = orderMapper.countByStatus(ORDER_CANCELLED);
        vo.setTotalPendingCount(totalPending);
        vo.setTotalPaidCount(totalPaid);
        vo.setTotalCancelledCount(totalCancelled);
        vo.setTotalOrderCount(totalPending + totalPaid + totalCancelled);
        vo.setTotalRevenue(orderMapper.sumPaidTotal());

        vo.setPerCinemaStats(cinemaMapper.selectCinemaStats());
        vo.setMovieRanking(movieMapper.selectTopMovies(MOVIE_RANK_LIMIT));
        vo.setDailyStats(fillMissingDays(orderMapper.selectDailyStats(todayStart.minusDays(DAILY_DAYS - 1L)), DAILY_DAYS));
        return vo;
    }

    /** 近 N 日补全为连续日期，无订单的日期补 0，保证图表横轴连续 */
    static List<DailyOrderStat> fillMissingDays(List<DailyOrderStat> rows, int days) {
        Map<String, DailyOrderStat> byDate = rows.stream()
                .collect(Collectors.toMap(DailyOrderStat::getDate, stat -> stat));
        List<DailyOrderStat> result = new ArrayList<>(days);
        LocalDate day = LocalDate.now().minusDays(days - 1L);
        for (int i = 0; i < days; i++) {
            result.add(byDate.getOrDefault(day.toString(), new DailyOrderStat(day.toString(), 0, BigDecimal.ZERO)));
            day = day.plusDays(1);
        }
        return result;
    }
}

package com.cdwater.cdticket.admin.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 仪表盘总览：统计卡片 + 分影院对比 + 影片票房排行 + 近 7 日订单/营收 */
@Data
public class DashboardVO {
    /** 热映：上架中且已上映（release_date ≤ 今天） */
    private long hotMovieCount;
    /** 待映：上架中且未上映（release_date > 今天） */
    private long upcomingMovieCount;
    /** 营业中的影院数（status=1） */
    private long cinemaOpenCount;
    /** 停业的影院数（status=0） */
    private long cinemaClosedCount;
    /** 用户总数（不论状态） */
    private long userCount;
    private long todayPendingCount;
    private long todayPaidCount;
    private long todayCancelledCount;
    /** 今日已支付订单营收 */
    private BigDecimal todayRevenue;
    /** 全部已支付订单营收 */
    private BigDecimal totalRevenue;
    private long totalOrderCount;
    private long totalPendingCount;
    private long totalPaidCount;
    private long totalCancelledCount;
    /** 各影院经营汇总（已支付营收降序） */
    private List<CinemaStat> perCinemaStats;
    /** 影片票房排行 Top 5（已支付营收降序） */
    private List<MovieRank> movieRanking;
    /** 近 7 日已支付订单/营收，缺失日期补 0（横轴连续） */
    private List<DailyOrderStat> dailyStats;
}

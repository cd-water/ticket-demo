package com.cdwater.cdticket.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.dto.dashboard.DailyOrderStat;
import com.cdwater.cdticket.admin.dto.order.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    IPage<OrderVO> selectPage(Page<OrderVO> page,
                              @Param("orderNo") Long orderNo,
                              @Param("status") Integer status,
                              @Param("cinemaId") Long cinemaId);

    /** 指定状态订单总数 */
    long countByStatus(@Param("status") int status);

    /** 某时间点之后指定状态订单数 */
    long countByStatusSince(@Param("status") int status, @Param("start") LocalDateTime start);

    /** 全部已支付订单营收 */
    BigDecimal sumPaidTotal();

    /** 某时间点之后已支付订单营收 */
    BigDecimal sumPaidSince(@Param("start") LocalDateTime start);

    /** 按日聚合已支付订单数/营收（无结果的天不返回，由 Service 补 0） */
    List<DailyOrderStat> selectDailyStats(@Param("start") LocalDateTime start);
}

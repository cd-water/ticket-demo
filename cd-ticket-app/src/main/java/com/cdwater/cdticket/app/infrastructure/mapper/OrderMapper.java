package com.cdwater.cdticket.app.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cdwater.cdticket.app.application.dto.OrderRowVO;
import com.cdwater.cdticket.app.application.dto.SeatPos;
import com.cdwater.cdticket.app.domain.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /** 我的订单分页（JOIN 电影/影院/影厅/排场），status 可空 */
    IPage<OrderRowVO> selectPageByUser(IPage<?> page, @Param("userId") Long userId,
                                       @Param("status") Integer status);

    /** 订单详情 JOIN 投影 */
    OrderRowVO selectRowById(@Param("id") Long id);

    /** 某排场已支付订单占用的座位（用于初始化已售位图） */
    @Select("""
            SELECT oi.seat_row, oi.seat_col
            FROM t_order o
            JOIN t_order_item oi ON oi.order_id = o.id
            WHERE o.screening_id = #{screeningId} AND o.status = 1
            """)
    List<SeatPos> selectSoldSeatsByScreening(@Param("screeningId") Long screeningId);
}

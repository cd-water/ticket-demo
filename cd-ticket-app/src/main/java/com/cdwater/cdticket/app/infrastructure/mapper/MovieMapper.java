package com.cdwater.cdticket.app.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.app.application.dto.BoxOfficeVO;
import com.cdwater.cdticket.app.domain.model.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MovieMapper extends BaseMapper<Movie> {

    /** 今日票房榜：已支付且今日支付的订单按电影聚合 */
    @Select("""
            SELECT m.id, m.title, SUM(o.total_amount) AS box_office
            FROM t_order o
            JOIN t_movie m ON o.movie_id = m.id
            WHERE o.status = 1 AND o.pay_time >= CURDATE()
            GROUP BY m.id, m.title
            ORDER BY box_office DESC
            LIMIT 10
            """)
    List<BoxOfficeVO> selectBoxOffice();
}

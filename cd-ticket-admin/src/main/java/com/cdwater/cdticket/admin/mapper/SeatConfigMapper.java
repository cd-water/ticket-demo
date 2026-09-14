package com.cdwater.cdticket.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.admin.entity.SeatConfig;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeatConfigMapper extends BaseMapper<SeatConfig> {
    /** 物理删除：uk_hall_row_col 不含 deleted，软删的行会挡住整表重写 */
    @Delete("DELETE FROM t_seat_config WHERE hall_id = #{hallId}")
    int deleteByHallId(@Param("hallId") Long hallId);
}

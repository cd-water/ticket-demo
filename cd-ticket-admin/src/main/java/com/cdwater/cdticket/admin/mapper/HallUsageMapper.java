package com.cdwater.cdticket.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HallUsageMapper {
    @Select("SELECT COUNT(*) FROM t_screening WHERE hall_id = #{hallId} AND deleted = 0")
    long countByHallId(@Param("hallId") Long hallId);
}

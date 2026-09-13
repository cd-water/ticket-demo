package com.cdwater.cdticket.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HallUsageMapper {

    /** 直查 screening 模块的 t_screening 表（只读引用计数；逻辑删除需手写条件） */
    @Select("SELECT COUNT(*) FROM t_screening WHERE hall_id = #{hallId} AND deleted = 0")
    long countByHallId(@Param("hallId") Long hallId);
}

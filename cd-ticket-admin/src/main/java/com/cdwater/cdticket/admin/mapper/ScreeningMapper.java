package com.cdwater.cdticket.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.admin.entity.Screening;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScreeningMapper extends BaseMapper<Screening> {
    /**
     * 物理删除：uk_hall_start 不含 deleted，软删的排场会堵住同影厅同时间的再次创建。
     * ponytail: 订单模块落地后需改为「有订单引用则拒绝删除」，否则 t_order.screening_id 悬空
     */
    @Delete("DELETE FROM t_screening WHERE id = #{id}")
    int deletePhysicallyById(@Param("id") Long id);
}

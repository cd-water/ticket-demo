package com.cdwater.cdticket.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.admin.dto.dashboard.CinemaStat;
import com.cdwater.cdticket.admin.entity.Cinema;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CinemaMapper extends BaseMapper<Cinema> {
    /** 各影院订单/营收/排场数（已支付营收降序，用于仪表盘分影院对比） */
    List<CinemaStat> selectCinemaStats();
}

package com.cdwater.cdticket.app.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.app.domain.model.Cinema;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CinemaMapper extends BaseMapper<Cinema> {
}

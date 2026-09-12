package com.cdwater.cdticket.cinema.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.cinema.infrastructure.entity.Cinema;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CinemaMapper extends BaseMapper<Cinema> {
}

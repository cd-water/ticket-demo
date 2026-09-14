package com.cdwater.cdticket.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.admin.dto.dashboard.MovieRank;
import com.cdwater.cdticket.admin.entity.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MovieMapper extends BaseMapper<Movie> {
    /** 影片票房排行（已支付营收降序，用于仪表盘） */
    List<MovieRank> selectTopMovies(@Param("limit") int limit);
}

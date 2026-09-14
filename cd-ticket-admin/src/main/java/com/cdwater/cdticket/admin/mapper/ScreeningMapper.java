package com.cdwater.cdticket.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.entity.Screening;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScreeningMapper extends BaseMapper<Screening> {
    IPage<ScreeningVO> selectVOPage(Page<ScreeningVO> page,
                                    @Param("cinemaId") Long cinemaId,
                                    @Param("movieId") Long movieId);

    /** 即将开场的排场（开场时间升序，用于仪表盘） */
    List<ScreeningVO> selectUpcoming(@Param("limit") int limit);
}

package com.cdwater.cdticket.screening.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.screening.infrastructure.entity.Screening;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScreeningMapper extends BaseMapper<Screening> {
}

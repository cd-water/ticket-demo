package com.cdwater.cdticket.app.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.app.domain.model.Screening;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScreeningMapper extends BaseMapper<Screening> {
}

package com.cdwater.cdticket.admin.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.admin.domain.entity.Screening;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScreeningMapper extends BaseMapper<Screening> {
}

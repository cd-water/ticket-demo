package com.cdwater.cdticket.admin.convert;

import com.cdwater.cdticket.admin.dto.screening.ScreeningSaveRequest;
import com.cdwater.cdticket.admin.entity.Screening;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScreeningConvert {

    ScreeningConvert INSTANCE = Mappers.getMapper(ScreeningConvert.class);

    Screening toEntity(ScreeningSaveRequest command);
}

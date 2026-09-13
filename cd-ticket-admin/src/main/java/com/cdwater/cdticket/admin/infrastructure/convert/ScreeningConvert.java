package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.screening.ScreeningSaveCommand;
import com.cdwater.cdticket.admin.infrastructure.entity.Screening;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScreeningConvert {

    ScreeningConvert INSTANCE = Mappers.getMapper(ScreeningConvert.class);

    Screening toEntity(ScreeningSaveCommand command);
}

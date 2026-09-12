package com.cdwater.cdticket.screening.infrastructure.convert;

import com.cdwater.cdticket.screening.application.dto.ScreeningSaveCommand;
import com.cdwater.cdticket.screening.infrastructure.entity.Screening;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScreeningConvert {

    ScreeningConvert INSTANCE = Mappers.getMapper(ScreeningConvert.class);

    Screening toEntity(ScreeningSaveCommand command);
}

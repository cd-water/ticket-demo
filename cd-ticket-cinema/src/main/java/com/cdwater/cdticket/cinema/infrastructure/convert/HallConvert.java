package com.cdwater.cdticket.cinema.infrastructure.convert;

import com.cdwater.cdticket.cinema.application.dto.HallSaveCommand;
import com.cdwater.cdticket.cinema.application.dto.HallVO;
import com.cdwater.cdticket.cinema.infrastructure.entity.Hall;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface HallConvert {

    HallConvert INSTANCE = Mappers.getMapper(HallConvert.class);

    HallVO toVO(Hall hall);

    Hall toEntity(HallSaveCommand command);
}

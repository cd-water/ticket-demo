package com.cdwater.cdticket.cinema.infrastructure.convert;

import com.cdwater.cdticket.cinema.application.dto.CinemaSaveCommand;
import com.cdwater.cdticket.cinema.application.dto.CinemaVO;
import com.cdwater.cdticket.cinema.infrastructure.entity.Cinema;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CinemaConvert {

    CinemaConvert INSTANCE = Mappers.getMapper(CinemaConvert.class);

    CinemaVO toVO(Cinema cinema);

    Cinema toEntity(CinemaSaveCommand command);
}

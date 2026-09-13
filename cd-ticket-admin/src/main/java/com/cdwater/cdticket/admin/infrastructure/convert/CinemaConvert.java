package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.cinema.CinemaSaveCommand;
import com.cdwater.cdticket.admin.application.dto.cinema.CinemaVO;
import com.cdwater.cdticket.admin.infrastructure.entity.Cinema;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CinemaConvert {

    CinemaConvert INSTANCE = Mappers.getMapper(CinemaConvert.class);

    CinemaVO toVO(Cinema cinema);

    Cinema toEntity(CinemaSaveCommand command);
}

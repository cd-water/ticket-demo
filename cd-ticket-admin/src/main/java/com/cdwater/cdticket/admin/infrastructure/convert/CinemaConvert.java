package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.CinemaSaveCommand;
import com.cdwater.cdticket.admin.application.dto.CinemaVO;
import com.cdwater.cdticket.admin.domain.entity.Cinema;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CinemaConvert {

    CinemaConvert INSTANCE = Mappers.getMapper(CinemaConvert.class);

    CinemaVO toVO(Cinema cinema);

    Cinema toEntity(CinemaSaveCommand command);
}

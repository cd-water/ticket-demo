package com.cdwater.cdticket.admin.convert;

import com.cdwater.cdticket.admin.dto.cinema.CinemaSaveRequest;
import com.cdwater.cdticket.admin.dto.cinema.CinemaVO;
import com.cdwater.cdticket.admin.entity.Cinema;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CinemaConvert {

    CinemaConvert INSTANCE = Mappers.getMapper(CinemaConvert.class);

    CinemaVO toVO(Cinema cinema);

    Cinema toEntity(CinemaSaveRequest command);
}

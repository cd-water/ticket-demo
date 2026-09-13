package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.hall.HallSaveCommand;
import com.cdwater.cdticket.admin.application.dto.hall.HallVO;
import com.cdwater.cdticket.admin.infrastructure.entity.Hall;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface HallConvert {

    HallConvert INSTANCE = Mappers.getMapper(HallConvert.class);

    HallVO toVO(Hall hall);

    Hall toEntity(HallSaveCommand command);
}

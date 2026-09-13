package com.cdwater.cdticket.admin.convert;

import com.cdwater.cdticket.admin.dto.hall.HallSaveRequest;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.entity.Hall;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface HallConvert {

    HallConvert INSTANCE = Mappers.getMapper(HallConvert.class);

    HallVO toVO(Hall hall);

    Hall toEntity(HallSaveRequest command);
}

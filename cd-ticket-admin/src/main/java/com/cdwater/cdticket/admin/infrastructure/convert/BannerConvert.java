package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.BannerSaveCommand;
import com.cdwater.cdticket.admin.application.dto.BannerVO;
import com.cdwater.cdticket.admin.domain.entity.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BannerConvert {

    BannerConvert INSTANCE = Mappers.getMapper(BannerConvert.class);

    BannerVO toVO(Banner banner);

    Banner toEntity(BannerSaveCommand command);
}

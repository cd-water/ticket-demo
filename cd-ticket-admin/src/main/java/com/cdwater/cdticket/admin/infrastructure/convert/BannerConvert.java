package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.banner.BannerSaveCommand;
import com.cdwater.cdticket.admin.application.dto.banner.BannerVO;
import com.cdwater.cdticket.admin.infrastructure.entity.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BannerConvert {

    BannerConvert INSTANCE = Mappers.getMapper(BannerConvert.class);

    BannerVO toVO(Banner banner);

    Banner toEntity(BannerSaveCommand command);
}

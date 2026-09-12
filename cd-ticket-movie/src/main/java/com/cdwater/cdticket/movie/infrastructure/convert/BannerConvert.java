package com.cdwater.cdticket.movie.infrastructure.convert;

import com.cdwater.cdticket.movie.application.dto.BannerSaveCommand;
import com.cdwater.cdticket.movie.application.dto.BannerVO;
import com.cdwater.cdticket.movie.infrastructure.entity.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BannerConvert {

    BannerConvert INSTANCE = Mappers.getMapper(BannerConvert.class);

    BannerVO toVO(Banner banner);

    Banner toEntity(BannerSaveCommand command);
}

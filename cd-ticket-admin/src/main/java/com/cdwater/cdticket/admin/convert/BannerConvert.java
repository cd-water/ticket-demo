package com.cdwater.cdticket.admin.convert;

import com.cdwater.cdticket.admin.dto.banner.BannerSaveRequest;
import com.cdwater.cdticket.admin.dto.banner.BannerVO;
import com.cdwater.cdticket.admin.entity.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BannerConvert {

    BannerConvert INSTANCE = Mappers.getMapper(BannerConvert.class);

    BannerVO toVO(Banner banner);

    Banner toEntity(BannerSaveRequest command);
}

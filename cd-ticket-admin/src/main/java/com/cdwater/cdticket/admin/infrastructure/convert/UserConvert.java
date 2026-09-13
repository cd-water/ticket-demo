package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.UserAdminVO;
import com.cdwater.cdticket.admin.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserAdminVO toAdminVO(User user);
}
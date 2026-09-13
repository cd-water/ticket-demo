package com.cdwater.cdticket.admin.convert;

import com.cdwater.cdticket.admin.dto.user.UserAdminVO;
import com.cdwater.cdticket.admin.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserAdminVO toAdminVO(User user);
}
package com.cdwater.cdticket.app.user.infrastructure.convert;

import com.cdwater.cdticket.app.user.application.dto.UserInfo;
import com.cdwater.cdticket.app.user.infrastructure.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    @Mapping(target = "hasPassword", expression = "java(user.getPassword() != null)")
    UserInfo toUserInfo(User user);
}
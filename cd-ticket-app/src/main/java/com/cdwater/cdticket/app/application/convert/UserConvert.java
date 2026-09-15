package com.cdwater.cdticket.app.application.convert;

import com.cdwater.cdticket.app.application.dto.UserInfo;
import com.cdwater.cdticket.app.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserInfo toUserInfo(User user);
}
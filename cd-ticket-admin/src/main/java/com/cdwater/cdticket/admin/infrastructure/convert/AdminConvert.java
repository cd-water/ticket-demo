package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.AdminInfo;
import com.cdwater.cdticket.admin.application.dto.AdminManageVO;
import com.cdwater.cdticket.admin.domain.entity.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminConvert {

    AdminConvert INSTANCE = Mappers.getMapper(AdminConvert.class);

    AdminInfo toAdminInfo(Admin admin);

    AdminManageVO toManageVO(Admin admin);
}
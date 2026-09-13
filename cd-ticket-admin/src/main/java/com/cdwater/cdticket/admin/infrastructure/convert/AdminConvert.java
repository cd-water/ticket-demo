package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.admin.AdminInfo;
import com.cdwater.cdticket.admin.application.dto.admin.AdminManageVO;
import com.cdwater.cdticket.admin.infrastructure.entity.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminConvert {

    AdminConvert INSTANCE = Mappers.getMapper(AdminConvert.class);

    AdminInfo toAdminInfo(Admin admin);

    AdminManageVO toManageVO(Admin admin);
}
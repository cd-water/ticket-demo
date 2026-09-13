package com.cdwater.cdticket.admin.convert;

import com.cdwater.cdticket.admin.dto.admin.AdminInfo;
import com.cdwater.cdticket.admin.dto.admin.AdminManageVO;
import com.cdwater.cdticket.admin.entity.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminConvert {

    AdminConvert INSTANCE = Mappers.getMapper(AdminConvert.class);

    AdminInfo toAdminInfo(Admin admin);

    AdminManageVO toManageVO(Admin admin);
}
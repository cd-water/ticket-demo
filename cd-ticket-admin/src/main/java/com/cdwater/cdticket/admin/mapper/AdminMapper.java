package com.cdwater.cdticket.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdwater.cdticket.admin.dto.admin.AdminVO;
import com.cdwater.cdticket.admin.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
    List<AdminVO> selectListAll();
}

package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 管理员认证鉴权
 */
@Service
@RequiredArgsConstructor
public class AdminAuthorizer {

    private final AdminMapper adminMapper;

    /**
     * 当前登录管理员
     */
    public AdminPrincipal currentAdmin() {
        Long adminId = SecurityUtils.getCurrentId();
        Admin admin = adminMapper.selectById(adminId);
        return new AdminPrincipal(admin.getId(), admin.getUsername(), admin.getRole(), admin.getCinemaId());
    }

    /**
     * 仅平台管理员通过，否则 FORBIDDEN
     */
    public void requirePlatformAdmin() {
        if (currentAdmin().getRole() != 0) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    /**
     * 仅影院管理员通过，否则 FORBIDDEN
     */
    public void requireCinemaAdmin() {
        if (currentAdmin().getRole() != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    /**
     * 平台管理员恒通过；影院管理员必须匹配 cinemaId，否则 FORBIDDEN
     */
    public void requireScope(long cinemaId) {
        AdminPrincipal cur = currentAdmin();
        if (cur.getRole() != 0 && !cur.getCinemaId().equals(cinemaId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }
}

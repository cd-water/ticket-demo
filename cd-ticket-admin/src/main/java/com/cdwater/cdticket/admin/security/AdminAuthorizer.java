package com.cdwater.cdticket.admin.security;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthorizer {

    private final AdminMapper adminMapper;

    /** 当前登录管理员；未登录 C002、已禁用 C002（消息「账号已禁用」） */
    public AdminPrincipal currentAdmin() {
        Admin admin = resolveAdmin();
        return new AdminPrincipal(admin.getId(), admin.getUsername(), admin.getRole(), admin.getCinemaId());
    }

    /** 非超级管理员抛 FORBIDDEN(C003) */
    public void requireSuperAdmin() {
        if (currentAdmin().getRole() != 0) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    /** 非影院管理员抛 FORBIDDEN(C003) */
    public void requireCinemaAdmin() {
        if (currentAdmin().getRole() != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    /** 超管恒通过；影院管理员要求与管辖 cinemaId 相等，否则 FORBIDDEN(C003) */
    public void requireScope(long cinemaId) {
        AdminPrincipal cur = currentAdmin();
        if (cur.getRole() != 0 && !cur.getCinemaId().equals(cinemaId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    /** 从 SecurityContext 取 id → 查库 → 校验启用。禁用后下一请求即被拒（管理员禁用即踢）。 */
    protected Admin resolveAdmin() {
        Long adminId = SecurityUtils.getCurrentId();
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BizException("账号已禁用", ResultCode.UNAUTHORIZED.getCode());
        }
        return admin;
    }
}

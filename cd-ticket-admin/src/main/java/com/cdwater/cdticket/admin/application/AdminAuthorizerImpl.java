package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.infrastructure.entity.Admin;
import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.admin.AdminPrincipal;
import com.cdwater.cdticket.common.api.ResultCode;
import com.cdwater.cdticket.common.exception.BizException;
import com.cdwater.cdticket.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthorizerImpl implements AdminAuthorizer {

    private final AdminRepository adminRepository;

    @Override
    public AdminPrincipal currentAdmin() {
        Admin admin = resolveAdmin();
        return new AdminPrincipal(admin.getId(), admin.getUsername(), admin.getRole(), admin.getCinemaId());
    }

    @Override
    public void requireSuperAdmin() {
        if (currentAdmin().getRole() != 0) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    @Override
    public void requireCinemaAdmin() {
        if (currentAdmin().getRole() != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    @Override
    public void requireScope(long cinemaId) {
        AdminPrincipal cur = currentAdmin();
        if (cur.getRole() != 0 && !cur.getCinemaId().equals(cinemaId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    /** 从 SecurityContext 取 id → 查库 → 校验启用。禁用后下一请求即被拒（管理员禁用即踢）。 */
    protected Admin resolveAdmin() {
        Long adminId = SecurityUtils.getCurrentId();
        Admin admin = adminRepository.findById(adminId);
        if (admin == null || admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BizException("账号已禁用", ResultCode.UNAUTHORIZED.getCode());
        }
        return admin;
    }
}

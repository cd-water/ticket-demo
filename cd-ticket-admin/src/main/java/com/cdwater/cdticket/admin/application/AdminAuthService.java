package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.application.dto.AdminLoginResponse;
import com.cdwater.cdticket.admin.common.api.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.domain.entity.Admin;
import com.cdwater.cdticket.admin.infrastructure.convert.AdminConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final TokenStoreService tokenStoreService;
    private final PasswordEncoder passwordEncoder;

    public AdminLoginResponse login(String username, String password) {
        Admin admin = adminRepository.findByUsername(username);
        if (admin == null || admin.getStatus() == null || admin.getStatus() != 1
                || !passwordEncoder.matches(password, admin.getPassword())) {
            // 三种失败（账号不存在 / 账号已禁用 / 密码错误）统一对外，避免用户名枚举攻击
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        String token = tokenStoreService.issue(admin.getId());

        AdminLoginResponse resp = new AdminLoginResponse();
        resp.setToken(token);
        resp.setAdmin(AdminConvert.INSTANCE.toAdminInfo(admin));
        return resp;
    }

    public void logout(Long adminId) {
        tokenStoreService.revoke(adminId);
    }
}
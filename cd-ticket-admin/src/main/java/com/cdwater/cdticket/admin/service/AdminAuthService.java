package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.convert.AdminConvert;
import com.cdwater.cdticket.admin.dto.admin.AdminLoginResponse;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import com.cdwater.cdticket.admin.security.TokenStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminMapper adminMapper;
    private final TokenStoreService tokenStoreService;
    private final PasswordEncoder passwordEncoder;

    public AdminLoginResponse login(String username, String password) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
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

    public void logout() {
        tokenStoreService.revoke(SecurityUtils.getCurrentId());
    }
}

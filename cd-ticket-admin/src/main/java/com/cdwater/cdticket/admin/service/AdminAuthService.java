package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.admin.LoginResponse;
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

    public LoginResponse login(String username, String password) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (admin == null || admin.getStatus() == null || admin.getStatus() != 1
                || !passwordEncoder.matches(password, admin.getPassword())) {
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        String token = tokenStoreService.issue(admin.getId());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        LoginResponse.AdminInfo info = new LoginResponse.AdminInfo();
        info.setId(admin.getId());
        info.setUsername(admin.getUsername());
        info.setRole(admin.getRole());
        info.setCinemaId(admin.getCinemaId());
        resp.setAdmin(info);
        return resp;
    }

    public void logout() {
        tokenStoreService.revoke(SecurityUtils.getCurrentId());
    }
}

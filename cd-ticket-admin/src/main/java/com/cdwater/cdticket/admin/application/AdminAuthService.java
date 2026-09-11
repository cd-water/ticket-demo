package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.application.AdminAuthDtos.AdminInfo;
import com.cdwater.cdticket.admin.application.AdminAuthDtos.AdminLoginResponse;
import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.infrastructure.entity.Admin;
import com.cdwater.cdticket.common.application.BizException;
import com.cdwater.cdticket.common.application.ResultCode;
import com.cdwater.cdticket.common.infrastructure.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final TokenStoreService tokenStoreService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthService(AdminRepository adminRepository, TokenStoreService tokenStoreService,
                            JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.tokenStoreService = tokenStoreService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminLoginResponse login(String username, String password) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BizException(ResultCode.ADMIN_NOT_FOUND));
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BizException(ResultCode.ADMIN_NOT_FOUND);
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BizException(ResultCode.ADMIN_PASSWORD_ERROR);
        }
        String token = jwtUtil.createAdminAccessToken(admin.getId());
        tokenStoreService.store(admin.getId(), token);
        return new AdminLoginResponse(token, AdminInfo.from(admin));
    }

    public void logout(Long adminId) {
        tokenStoreService.remove(adminId);
    }
}

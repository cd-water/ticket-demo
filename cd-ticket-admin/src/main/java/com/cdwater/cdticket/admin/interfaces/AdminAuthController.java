package com.cdwater.cdticket.admin.interfaces;

import com.cdwater.cdticket.admin.application.AdminAuthService;
import com.cdwater.cdticket.admin.application.dto.AdminLoginResponse;
import com.cdwater.cdticket.admin.interfaces.dto.LoginRequest;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.common.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return Result.success(adminAuthService.login(req.getUsername(), req.getPassword()));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        adminAuthService.logout(SecurityUtils.getCurrentId());
        return Result.success();
    }
}
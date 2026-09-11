package com.cdwater.cdticket.admin.interfaces;

import com.cdwater.cdticket.admin.application.AdminAuthDtos.AdminLoginResponse;
import com.cdwater.cdticket.admin.application.AdminAuthService;
import com.cdwater.cdticket.common.domain.Result;
import com.cdwater.cdticket.common.interfaces.SecurityUtils;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    public record LoginRequest(@NotBlank(message = "用户名不能为空") String username,
                               @NotBlank(message = "密码不能为空") String password) {}

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody LoginRequest req) {
        return Result.ok(adminAuthService.login(req.username(), req.password()));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        adminAuthService.logout(SecurityUtils.getCurrentId());
        return Result.ok();
    }
}

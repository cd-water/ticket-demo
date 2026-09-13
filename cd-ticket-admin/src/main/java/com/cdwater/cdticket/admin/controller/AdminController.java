package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.AdminAuthService;
import com.cdwater.cdticket.admin.security.AdminAuthorizer;
import com.cdwater.cdticket.admin.service.AdminManageService;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.admin.AdminLoginResponse;
import com.cdwater.cdticket.admin.dto.admin.AdminManageVO;
import com.cdwater.cdticket.admin.dto.admin.AdminSaveRequest;
import com.cdwater.cdticket.admin.dto.admin.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员登录/登出 + 账号管理
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final AdminManageService adminManageService;
    private final AdminAuthorizer adminAuthorizer;

    @PostMapping("/auth/login")
    public Result<AdminLoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return Result.success(adminAuthService.login(req.getUsername(), req.getPassword()));
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        adminAuthService.logout();
        return Result.success();
    }

    @GetMapping("/admins/list")
    public Result<List<AdminManageVO>> list(@RequestParam(required = false) Integer role) {
        return Result.success(adminManageService.list(role));
    }

    @PostMapping("/admins/save")
    public Result<Void> save(@RequestBody @Valid AdminSaveRequest req) {
        adminManageService.save(req);
        return Result.success();
    }

    @PostMapping("/admins/{id}/delete")
    public Result<Void> delete(@PathVariable Long id) {
        adminManageService.delete(id);
        return Result.success();
    }
}

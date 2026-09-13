package com.cdwater.cdticket.admin.interfaces.controller;

import com.cdwater.cdticket.admin.application.AdminAuthService;
import com.cdwater.cdticket.admin.application.AdminAuthorizer;
import com.cdwater.cdticket.admin.application.AdminManageService;
import com.cdwater.cdticket.admin.application.dto.Result;
import com.cdwater.cdticket.admin.application.dto.admin.AdminCreateCommand;
import com.cdwater.cdticket.admin.application.dto.admin.AdminLoginResponse;
import com.cdwater.cdticket.admin.application.dto.admin.AdminManageVO;
import com.cdwater.cdticket.admin.application.dto.admin.AdminUpdateCommand;
import com.cdwater.cdticket.admin.interfaces.dto.admin.AdminCreateRequest;
import com.cdwater.cdticket.admin.interfaces.dto.admin.AdminUpdateRequest;
import com.cdwater.cdticket.admin.interfaces.dto.admin.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 管理员：登录/登出 + 账号管理 */
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

    @GetMapping("/admins")
    public Result<List<AdminManageVO>> list(@RequestParam(required = false) Integer role) {
        return Result.success(adminManageService.list(role));
    }

    @PostMapping("/admins")
    public Result<Void> create(@RequestBody @Valid AdminCreateRequest req) {
        adminManageService.create(new AdminCreateCommand(req.getUsername(), req.getPassword(),
                req.getRole(), req.getCinemaId()));
        return Result.success();
    }

    @PutMapping("/admins/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid AdminUpdateRequest req) {
        adminAuthorizer.requireSuperAdmin();
        adminManageService.update(id, new AdminUpdateCommand(req.getUsername(), req.getRole(),
                req.getCinemaId(), req.getStatus()));
        return Result.success();
    }

    @DeleteMapping("/admins/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requireSuperAdmin();
        adminManageService.delete(id);
        return Result.success();
    }
}

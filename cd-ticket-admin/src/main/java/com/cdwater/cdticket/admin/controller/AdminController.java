package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.AdminAuthService;
import com.cdwater.cdticket.admin.service.AdminManageService;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.admin.LoginResponse;
import com.cdwater.cdticket.admin.dto.admin.AdminVO;
import com.cdwater.cdticket.admin.dto.admin.AdminSaveRequest;
import com.cdwater.cdticket.admin.dto.admin.LoginRequest;
import com.cdwater.cdticket.admin.dto.admin.ResetPasswordRequest;
import com.cdwater.cdticket.admin.dto.common.StatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminAuthService adminAuthService;
    private final AdminManageService adminManageService;

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return Result.success(adminAuthService.login(req.getUsername(), req.getPassword()));
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        adminAuthService.logout();
        return Result.success();
    }

    @GetMapping("/admins/list")
    public Result<List<AdminVO>> list() {
        return Result.success(adminManageService.list());
    }

    @PostMapping("/admins/create")
    public Result<Void> create(@RequestBody @Valid AdminSaveRequest req) {
        adminManageService.create(req);
        return Result.success();
    }

    @PostMapping("/admins/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody @Valid ResetPasswordRequest req) {
        adminManageService.resetPassword(id, req);
        return Result.success();
    }

    @PostMapping("/admins/{id}/delete")
    public Result<Void> delete(@PathVariable Long id) {
        adminManageService.delete(id);
        return Result.success();
    }

    @PostMapping("/admins/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody @Valid StatusRequest req) {
        adminManageService.toggleStatus(id, req.getStatus());
        return Result.success();
    }
}
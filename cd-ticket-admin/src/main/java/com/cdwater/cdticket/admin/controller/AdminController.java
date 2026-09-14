package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.AuthService;
import com.cdwater.cdticket.admin.service.AdminService;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.admin.LoginResponse;
import com.cdwater.cdticket.admin.dto.admin.AdminVO;
import com.cdwater.cdticket.admin.dto.admin.AdminSaveRequest;
import com.cdwater.cdticket.admin.dto.admin.LoginRequest;
import com.cdwater.cdticket.admin.dto.admin.ResetPasswordRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AuthService authService;
    private final AdminService adminService;

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return Result.success(authService.login(req.getUsername(), req.getPassword()));
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @GetMapping("/admins/list")
    public Result<List<AdminVO>> list() {
        return Result.success(adminService.list());
    }

    @PostMapping("/admins/create")
    public Result<Void> create(@RequestBody @Valid AdminSaveRequest req) {
        adminService.create(req);
        return Result.success();
    }

    @PostMapping("/admins/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody @Valid ResetPasswordRequest req) {
        adminService.resetPassword(id, req);
        return Result.success();
    }

    @PostMapping("/admins/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam @Min(0) @Max(1) Integer status) {
        adminService.toggleStatus(id, status);
        return Result.success();
    }
}
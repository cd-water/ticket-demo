package com.cdwater.cdticket.admin.interfaces.controller;

import com.cdwater.cdticket.admin.application.AdminAuthorizer;
import com.cdwater.cdticket.admin.application.UserAdminService;
import com.cdwater.cdticket.admin.application.dto.PageResult;
import com.cdwater.cdticket.admin.application.dto.Result;
import com.cdwater.cdticket.admin.application.dto.user.UserAdminVO;
import com.cdwater.cdticket.admin.interfaces.dto.user.UserStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserAdminService userAdminService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<PageResult<UserAdminVO>> page(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String phone) {
        adminAuthorizer.requireSuperAdmin();
        return Result.success(userAdminService.page(page, size, phone));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid UserStatusRequest req) {
        adminAuthorizer.requireSuperAdmin();
        userAdminService.updateStatus(id, req.getStatus());
        return Result.success();
    }
}
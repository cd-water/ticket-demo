package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.UserService;
import com.cdwater.cdticket.admin.dto.user.UserAdminVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.user.UserStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public Result<PageResult<UserAdminVO>> page(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String phone) {
        return Result.success(userService.page(page, size, phone));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid UserStatusRequest req) {
        userService.updateStatus(id, req.getStatus());
        return Result.success();
    }
}
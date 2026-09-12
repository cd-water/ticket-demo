package com.cdwater.cdticket.user.interfaces;

import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.api.Result;
import com.cdwater.cdticket.user.application.AdminUserService;
import com.cdwater.cdticket.user.application.dto.UserAdminVO;
import com.cdwater.cdticket.user.interfaces.dto.UserStatusRequest;
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

    private final AdminUserService adminUserService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<PageResult<UserAdminVO>> page(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String phone) {
        adminAuthorizer.requireSuperAdmin();
        return Result.success(adminUserService.page(page, size, phone));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid UserStatusRequest req) {
        adminAuthorizer.requireSuperAdmin();
        adminUserService.updateStatus(id, req.getStatus());
        return Result.success();
    }
}

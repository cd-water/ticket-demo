package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.UserService;
import com.cdwater.cdticket.admin.dto.user.UserVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<PageResult<UserVO>> page(@RequestParam(defaultValue = "1") @Min(1) int page,
                                           @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                                           @RequestParam(required = false) @Size(max = 20) String phone,
                                           @RequestParam(required = false) @Min(0) @Max(1) Integer status) {
        return Result.success(userService.page(page, size, phone, status));
    }

    @PostMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam @Min(0) @Max(1) Integer status) {
        userService.toggleStatus(id, status);
        return Result.success();
    }
}

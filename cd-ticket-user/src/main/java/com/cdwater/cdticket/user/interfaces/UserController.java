package com.cdwater.cdticket.user.interfaces;

import com.cdwater.cdticket.common.api.Result;
import com.cdwater.cdticket.common.security.SecurityUtils;
import com.cdwater.cdticket.user.application.AuthService;
import com.cdwater.cdticket.user.application.dto.UserInfo;
import com.cdwater.cdticket.user.interfaces.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public Result<UserInfo> me() {
        return Result.success(authService.getMe(SecurityUtils.getCurrentId()));
    }

    @PostMapping("/me/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordRequest req) {
        authService.changePassword(SecurityUtils.getCurrentId(), req.getNewPassword(), req.getConfirmPassword());
        return Result.success();
    }
}
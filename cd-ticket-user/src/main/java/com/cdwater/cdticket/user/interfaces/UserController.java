package com.cdwater.cdticket.user.interfaces;

import com.cdwater.cdticket.common.domain.Result;
import com.cdwater.cdticket.common.interfaces.SecurityUtils;
import com.cdwater.cdticket.user.application.AuthDtos.UserInfo;
import com.cdwater.cdticket.user.application.AuthService;
import com.cdwater.cdticket.user.interfaces.UserRequestDtos.ChangePasswordRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public Result<UserInfo> me() {
        return Result.ok(authService.getMe(SecurityUtils.getCurrentId()));
    }

    @PostMapping("/me/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordRequest req) {
        authService.changePassword(SecurityUtils.getCurrentId(), req.newPassword(), req.confirmPassword());
        return Result.ok();
    }
}

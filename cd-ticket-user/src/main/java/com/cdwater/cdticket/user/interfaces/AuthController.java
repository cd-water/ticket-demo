package com.cdwater.cdticket.user.interfaces;

import com.cdwater.cdticket.common.domain.Result;
import com.cdwater.cdticket.user.application.AuthDtos.LoginResponse;
import com.cdwater.cdticket.user.application.AuthDtos.RefreshResponse;
import com.cdwater.cdticket.user.application.AuthService;
import com.cdwater.cdticket.user.interfaces.UserRequestDtos.LogoutRequest;
import com.cdwater.cdticket.user.interfaces.UserRequestDtos.PasswordLoginRequest;
import com.cdwater.cdticket.user.interfaces.UserRequestDtos.RefreshRequest;
import com.cdwater.cdticket.user.interfaces.UserRequestDtos.SmsCodeRequest;
import com.cdwater.cdticket.user.interfaces.UserRequestDtos.SmsLoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sms-code")
    public Result<Void> smsCode(@RequestBody @Valid SmsCodeRequest req) {
        authService.sendCode(req.phone());
        return Result.ok();
    }

    @PostMapping("/login/sms")
    public Result<LoginResponse> loginSms(@RequestBody @Valid SmsLoginRequest req) {
        return Result.ok(authService.loginBySms(req.phone(), req.code()));
    }

    @PostMapping("/login/password")
    public Result<LoginResponse> loginPassword(@RequestBody @Valid PasswordLoginRequest req) {
        return Result.ok(authService.loginByPassword(req.phone(), req.password()));
    }

    @PostMapping("/refresh")
    public Result<RefreshResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        return Result.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody @Valid LogoutRequest req) {
        authService.logout(req.refreshToken());
        return Result.ok();
    }
}

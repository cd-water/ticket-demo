package com.cdwater.cdticket.user.interfaces;

import com.cdwater.cdticket.common.api.Result;
import com.cdwater.cdticket.user.application.AuthService;
import com.cdwater.cdticket.user.application.dto.LoginResponse;
import com.cdwater.cdticket.user.application.dto.RefreshResponse;
import com.cdwater.cdticket.user.interfaces.dto.ChangePasswordRequest;
import com.cdwater.cdticket.user.interfaces.dto.LogoutRequest;
import com.cdwater.cdticket.user.interfaces.dto.PasswordLoginRequest;
import com.cdwater.cdticket.user.interfaces.dto.RefreshRequest;
import com.cdwater.cdticket.user.interfaces.dto.SmsCodeRequest;
import com.cdwater.cdticket.user.interfaces.dto.SmsLoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms-code")
    public Result<Void> smsCode(@RequestBody @Valid SmsCodeRequest req) {
        authService.sendCode(req.getPhone());
        return Result.success();
    }

    @PostMapping("/login/sms")
    public Result<LoginResponse> loginSms(@RequestBody @Valid SmsLoginRequest req) {
        return Result.success(authService.loginBySms(req.getPhone(), req.getCode()));
    }

    @PostMapping("/login/password")
    public Result<LoginResponse> loginPassword(@RequestBody @Valid PasswordLoginRequest req) {
        return Result.success(authService.loginByPassword(req.getPhone(), req.getPassword()));
    }

    @PostMapping("/refresh")
    public Result<RefreshResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        return Result.success(authService.refresh(req.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody @Valid LogoutRequest req) {
        authService.logout(req.getRefreshToken());
        return Result.success();
    }
}
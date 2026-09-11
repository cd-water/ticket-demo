package com.cdwater.cdticket.user.interfaces;

import jakarta.validation.constraints.NotBlank;

public record UserRequestDtos() {
    public record SmsCodeRequest(@NotBlank(message = "手机号不能为空") String phone) {}
    public record SmsLoginRequest(@NotBlank(message = "手机号不能为空") String phone,
                                  @NotBlank(message = "验证码不能为空") String code) {}
    public record PasswordLoginRequest(@NotBlank(message = "手机号不能为空") String phone,
                                       @NotBlank(message = "密码不能为空") String password) {}
    public record RefreshRequest(@NotBlank(message = "refreshToken不能为空") String refreshToken) {}
    public record LogoutRequest(@NotBlank(message = "refreshToken不能为空") String refreshToken) {}
    public record ChangePasswordRequest(@NotBlank(message = "新密码不能为空") String newPassword,
                                        @NotBlank(message = "确认密码不能为空") String confirmPassword) {}
}

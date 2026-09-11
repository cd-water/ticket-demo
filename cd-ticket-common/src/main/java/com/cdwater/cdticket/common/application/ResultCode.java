package com.cdwater.cdticket.common.application;

public enum ResultCode {
    SUCCESS(0, "ok"),
    BAD_REQUEST(1001, "参数错误"),
    UNAUTHORIZED(1002, "未认证或登录已过期"),
    FORBIDDEN(1003, "无权限"),
    SMS_CODE_INVALID(2001, "验证码错误或已过期"),
    SMS_SEND_TOO_FREQUENT(2002, "验证码发送过于频繁"),
    PHONE_INVALID(2003, "手机号格式非法"),
    USER_DISABLED(2101, "用户已禁用"),
    LOGIN_FAILED(2102, "账号或密码错误"),
    PASSWORD_SAME_AS_OLD(2103, "新密码与旧密码相同"),
    PASSWORD_CONFIRM_MISMATCH(2104, "两次输入密码不一致"),
    ADMIN_NOT_FOUND(2201, "管理员不存在或已禁用"),
    ADMIN_PASSWORD_ERROR(2202, "密码错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}

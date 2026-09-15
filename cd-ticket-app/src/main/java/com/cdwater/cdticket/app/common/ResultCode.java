package com.cdwater.cdticket.app.common;

import lombok.Getter;

/**
 * 错误码：O=成功，C=客户端错误，S=服务端错误，T=第三方错误
 */
@Getter
public enum ResultCode {
    SUCCESS("0000", "ok"),

    // 通用客户端错误
    BAD_REQUEST("C001", "Bad Request"),
    UNAUTHORIZED("C002", "Unauthorized"),
    FORBIDDEN("C003", "Forbidden"),
    NOT_FOUND("C004", "Not Found"),
    CONFLICT("C005", "Conflict"),

    // 认证与账号
    LOGIN_FAILED("C101", "用户名或密码错误"),
    SMS_CODE_INVALID("C102", "验证码错误或已过期"),
    PASSWORD_SAME_AS_OLD("C104", "新密码与旧密码相同"),
    PASSWORD_CONFIRM_MISMATCH("C105", "两次输入密码不一致"),

    // 服务端错误
    INTERNAL_ERROR("S001", "Internal Server Error"),

    // 第三方错误
    SMS_SERVICE_ERROR("T001", "短信服务异常");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

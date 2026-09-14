package com.cdwater.cdticket.app.common;

import lombok.Getter;

/**
 * 错误码
 *
 * 格式：[类别字母] + 3 位数字；SUCCESS 单独用 "0000"
 *   O = 成功
 *   C = 客户端错误
 *   S = 服务端错误
 *   T = 第三方错误（暂未使用）
 */
@Getter
public enum ResultCode {
    SUCCESS("0000", "ok"),

    // C0xx 公共
    BAD_REQUEST("C001", "参数错误"),
    UNAUTHORIZED("C002", "未认证或登录已过期"),

    // C1xx 用户
    LOGIN_FAILED("C101", "用户名或密码错误"),
    SMS_CODE_INVALID("C102", "验证码错误或已过期"),
    USER_DISABLED("C103", "用户已禁用"),
    PASSWORD_SAME_AS_OLD("C104", "新密码与旧密码相同"),
    PASSWORD_CONFIRM_MISMATCH("C105", "两次输入密码不一致"),

    // S0xx 公共
    INTERNAL_ERROR("S001", "系统异常");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

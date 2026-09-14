package com.cdwater.cdticket.admin.common;

import lombok.Getter;

/**
 * 业务错误码
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "请求成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "系统异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

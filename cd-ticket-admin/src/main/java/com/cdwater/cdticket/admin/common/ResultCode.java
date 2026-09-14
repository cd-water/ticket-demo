package com.cdwater.cdticket.admin.common;

import lombok.Getter;

/**
 * 业务错误码
 */
@Getter
public enum ResultCode {

    SUCCESS("0000", "ok"),

    BAD_REQUEST("C001", "参数错误"),
    UNAUTHORIZED("C002", "未认证或登录已过期"),
    FORBIDDEN("C003", "无权限"),
    NOT_FOUND("C004", "不存在"),

    ADMIN_USERNAME_EXISTS("C201", "用户名已存在"),
    LOGIN_FAILED("C204", "用户名或密码错误"),

    SCREENING_TIME_CONFLICT("C501", "同影厅同一开场时间已有排场"),
    HALL_HAS_SCREENING("C502", "该影厅已有排场，禁止删除"),
    SCREENING_STARTED("C503", "排场已开场，禁止修改/删除"),

    INTERNAL_ERROR("S001", "系统异常");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

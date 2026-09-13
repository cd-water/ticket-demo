package com.cdwater.cdticket.admin.common;

import lombok.Getter;

/**
 * 错误码：类别字母 + 3 位数字
 * 类别：O=成功，C=客户端错误，S=服务端错误
 * 示例：C001 参数错误，C501 排场冲突
 */
@Getter
public enum ResultCode {
    SUCCESS("0000", "ok"),

    // 客户端-公共
    BAD_REQUEST("C001", "参数错误"),
    UNAUTHORIZED("C002", "未认证或登录已过期"),
    FORBIDDEN("C003", "无权限"),
    LOGIN_FAILED("C004", "用户名或密码错误"),

    // 客户端-管理员
    ADMIN_USERNAME_EXISTS("C201", "用户名已存在"),
    CINEMA_ADMIN_NEED_CINEMA("C202", "影院管理员必须绑定影院"),
    CANNOT_OPERATE_SELF("C203", "不能操作当前登录管理员"),
    NOT_FOUND("C204", "记录不存在"),

    // 客户端-排场
    SCREENING_TIME_CONFLICT("C501", "同影厅同一开场时间已有排场"),
    HALL_HAS_SCREENING("C502", "该影厅已有排场，禁止删除"),
    SCREENING_STARTED("C503", "排场已开场，禁止修改/删除"),

    // 服务端-公共
    INTERNAL_ERROR("S001", "系统异常");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

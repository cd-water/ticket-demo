package com.cdwater.cdticket.common.api;

import lombok.Getter;

/**
 * 错误码（阿里巴巴异常体系规范，简短编码）
 *
 * 格式：<b>[类别字母]</b> + 3 位数字；SUCCESS 单独用 "0000"
 *   类别字母：
 *     O = 成功（恒为 "0000"）
 *     C = 客户端错误（4xx 等价）
 *     S = 服务端错误（5xx 等价）
 *     T = 第三方错误
 *   数字部分 (MSS)：
 *     M = 业务模块  0=公共, 1=用户, 2=管理员, 3=影片, 4=影院, 5=排场, 6=订单, 7=支付, 8=轮播图
 *     SS=模块内序号 01-99
 *
 * 第一位字母便于日志/抓包中快速识别异常类别：
 *   <b>O</b> 成功
 *   <b>C</b> 客户端参数或操作错误
 *   <b>S</b> 服务端系统错误
 *   <b>T</b> 第三方服务错误
 *
 * 示例：
 *   0000  = 成功
 *   C001  = 客户端-公共-001    参数错误
 *   C004  = 客户端-公共-004    用户名或密码错误
 *   C101  = 客户端-用户-101    验证码错误或已过期
 *   S001  = 服务端-公共-001    系统异常
 */
@Getter
public enum ResultCode {
    SUCCESS("0000", "ok"),

    // ========== Cxxx 客户端错误 ==========
    // C0xx 公共
    BAD_REQUEST("C001", "参数错误"),
    UNAUTHORIZED("C002", "未认证或登录已过期"),
    FORBIDDEN("C003", "无权限"),
    LOGIN_FAILED("C004", "用户名或密码错误"),

    // C1xx 用户
    SMS_CODE_INVALID("C101", "验证码错误或已过期"),
    USER_DISABLED("C102", "用户已禁用"),
    PASSWORD_SAME_AS_OLD("C103", "新密码与旧密码相同"),
    PASSWORD_CONFIRM_MISMATCH("C104", "两次输入密码不一致"),

    // C2xx 管理员
    ADMIN_USERNAME_EXISTS("C201", "用户名已存在"),
    CINEMA_ADMIN_NEED_CINEMA("C202", "影院管理员必须绑定影院"),
    CANNOT_OPERATE_SELF("C203", "不能操作当前登录管理员"),
    NOT_FOUND("C204", "记录不存在"),

    // C3xx 影片（预留）
    // C4xx 影院（预留）
    // C5xx 排场
    SCREENING_TIME_CONFLICT("C501", "同影厅同一开场时间已有排场"),
    HALL_HAS_SCREENING("C502", "该影厅已有排场，禁止删除"),
    SCREENING_STARTED("C503", "排场已开场，禁止修改/删除"),
    // C6xx 订单（预留）
    // C7xx 支付（预留）

    // ========== Sxxx 服务端错误 ==========
    // S0xx 公共
    INTERNAL_ERROR("S001", "系统异常");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
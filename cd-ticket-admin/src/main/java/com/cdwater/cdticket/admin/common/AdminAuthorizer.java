package com.cdwater.cdticket.admin.common;

/**
 * 管理端授权接口。实现位于 admin 模块（AdminAuthorizerImpl）。
 * 业务模块只依赖本接口，保持 Maven DAG 单向。
 */
public interface AdminAuthorizer {

    /** 当前登录管理员；未登录 C002、已禁用 C002（消息「账号已禁用」） */
    AdminPrincipal currentAdmin();

    /** 非超级管理员抛 FORBIDDEN(C003) */
    void requireSuperAdmin();

    /** 非影院管理员抛 FORBIDDEN(C003) */
    void requireCinemaAdmin();

    /** 超管恒通过；影院管理员要求与管辖 cinemaId 相等，否则 FORBIDDEN(C003) */
    void requireScope(long cinemaId);
}

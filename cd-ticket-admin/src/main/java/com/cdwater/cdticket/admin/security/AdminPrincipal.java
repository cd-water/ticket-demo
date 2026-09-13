package com.cdwater.cdticket.admin.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录管理员身份（不含持久化字段）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPrincipal {

    private Long id;
    private String username;
    private Integer role;
    private Long cinemaId;
}

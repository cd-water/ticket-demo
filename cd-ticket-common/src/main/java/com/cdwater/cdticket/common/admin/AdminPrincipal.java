package com.cdwater.cdticket.common.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 当前登录管理员的值对象（无持久化依赖，供所有业务模块使用） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPrincipal {

    private Long id;
    private String username;
    private Integer role;
    private Long cinemaId;
}

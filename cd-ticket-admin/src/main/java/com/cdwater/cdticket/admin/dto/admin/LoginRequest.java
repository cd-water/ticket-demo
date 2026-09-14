package com.cdwater.cdticket.admin.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @Size(min = 5, max = 32)
    private String username;

    /** 只做非空校验：强度规则属于新增/重置密码，在登录口校验会让库里的历史密码永远登不进来 */
    @NotBlank
    private String password;
}

package com.cdwater.cdticket.admin.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 重置密码请求 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码需8-20位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$", message = "密码需8-20位，且包含字母与数字")
    private String password;
}
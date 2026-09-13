package com.cdwater.cdticket.admin.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

/** 新增管理员请求 */
@Data
public class AdminSaveRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 5, max = 32, message = "用户名需5-32位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码需8-20位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$", message = "密码需8-20位，且包含字母与数字")
    private String password;

    @NotNull(message = "角色不能为空")
    @Min(0) @Max(1) private Integer role;

    private Long cinemaId;
}
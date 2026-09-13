package com.cdwater.cdticket.admin.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 新增/修改管理员请求（id=null → 新增，id!=null → 修改）
 */
@Data
public class AdminSaveRequest {

    /** 修改时传入；新增时不填 */
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 5, max = 32, message = "用户名需5-32位")
    private String username;

    /** 新增时必填；修改时不填（留空则不修改密码） */
    @Size(min = 8, max = 20, message = "密码需8-20位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$", message = "密码需8-20位，且包含字母与数字")
    private String password;

    @NotNull(message = "角色不能为空")
    @Min(value = 0, message = "角色值无效")
    @Max(value = 1, message = "角色值无效")
    private Integer role;

    private Long cinemaId;

    /** 仅修改时传入；新增时不填 */
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}

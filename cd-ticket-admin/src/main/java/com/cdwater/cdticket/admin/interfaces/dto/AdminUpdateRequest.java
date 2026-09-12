package com.cdwater.cdticket.admin.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminUpdateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 32, message = "用户名最多32字符")
    private String username;

    @NotNull(message = "角色不能为空")
    @Min(value = 0, message = "角色值无效")
    @Max(value = 1, message = "角色值无效")
    private Integer role;

    private Long cinemaId;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}

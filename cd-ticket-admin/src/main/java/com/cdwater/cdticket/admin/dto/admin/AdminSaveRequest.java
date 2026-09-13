package com.cdwater.cdticket.admin.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 新增管理员请求
 */
@Data
public class AdminSaveRequest {

    @Size(min = 5, max = 32)
    private String username;

    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$")
    private String password;

    @Min(0)
    @Max(1)
    private Integer role;

    private Long cinemaId;
}
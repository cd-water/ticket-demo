package com.cdwater.cdticket.admin.application.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAdminVO {
    private Long id;
    private String phone;
    private String nickname;
    private Integer status;
    private LocalDateTime createTime;
}
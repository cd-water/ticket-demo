package com.cdwater.cdticket.admin.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminVO {
    private Long id;
    private String username;
    private Integer role;
    private String cinemaName;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

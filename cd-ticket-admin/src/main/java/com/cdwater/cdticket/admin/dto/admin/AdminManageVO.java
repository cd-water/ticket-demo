package com.cdwater.cdticket.admin.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminManageVO {
    private Long id;
    private String username;
    private Integer role;
    private Long cinemaId;
    private Integer status;
    private LocalDateTime createTime;
}

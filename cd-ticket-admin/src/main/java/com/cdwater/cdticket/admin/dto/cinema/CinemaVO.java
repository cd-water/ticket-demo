package com.cdwater.cdticket.admin.dto.cinema;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CinemaVO {
    private Long id;
    private String name;
    private String address;
    private Integer status;
    private LocalDateTime createTime;
}

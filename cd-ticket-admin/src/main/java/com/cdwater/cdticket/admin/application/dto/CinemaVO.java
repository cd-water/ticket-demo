package com.cdwater.cdticket.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaVO {

    private Long id;
    private String name;
    private String address;
    private Integer status;
    private LocalDateTime createTime;
}
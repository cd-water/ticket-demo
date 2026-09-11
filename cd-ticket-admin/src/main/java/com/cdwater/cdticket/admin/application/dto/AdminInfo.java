package com.cdwater.cdticket.admin.application.dto;

import lombok.Data;

@Data
public class AdminInfo {

    private Long id;
    private String username;
    private Integer role;
    private Long cinemaId;
}
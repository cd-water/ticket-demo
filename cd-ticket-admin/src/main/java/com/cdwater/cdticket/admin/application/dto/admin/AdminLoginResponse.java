package com.cdwater.cdticket.admin.application.dto.admin;

import lombok.Data;

@Data
public class AdminLoginResponse {

    private String token;
    private AdminInfo admin;
}
package com.cdwater.cdticket.app.user.application.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserInfo user;
}
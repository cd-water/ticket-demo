package com.cdwater.cdticket.app.user.application.dto;

import lombok.Data;

@Data
public class RefreshResponse {

    private String accessToken;
    private String refreshToken;
}
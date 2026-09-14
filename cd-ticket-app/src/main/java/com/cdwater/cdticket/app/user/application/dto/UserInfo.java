package com.cdwater.cdticket.app.user.application.dto;

import lombok.Data;

@Data
public class UserInfo {

    private Long id;
    private String phone;
    private String nickname;
    private Boolean hasPassword;
}
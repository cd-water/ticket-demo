package com.cdwater.cdticket.admin.dto.admin;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private AdminInfo admin;

    @Data
    public static class AdminInfo {
        private Long id;
        private String username;
        private Integer role;
        private Long cinemaId;
    }
}

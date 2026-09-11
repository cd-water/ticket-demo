package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.infrastructure.entity.Admin;

public record AdminAuthDtos() {
    public record AdminLoginResponse(String token, AdminInfo admin) {}
    public record AdminInfo(Long id, String username, Integer role, Long cinemaId) {
        public static AdminInfo from(Admin admin) {
            return new AdminInfo(admin.getId(), admin.getUsername(), admin.getRole(), admin.getCinemaId());
        }
    }
}

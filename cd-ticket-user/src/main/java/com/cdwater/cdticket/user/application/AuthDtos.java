package com.cdwater.cdticket.user.application;

import com.cdwater.cdticket.user.infrastructure.entity.User;

public record AuthDtos() {
    public record LoginResponse(String accessToken, String refreshToken, UserInfo user) {}
    public record RefreshResponse(String accessToken, String refreshToken) {}
    public record UserInfo(Long id, String phone, String nickname, boolean hasPassword) {
        public static UserInfo from(User user) {
            return new UserInfo(user.getId(), user.getPhone(), user.getNickname(), user.getPassword() != null);
        }
    }
}

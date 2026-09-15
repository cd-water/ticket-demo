package com.cdwater.cdticket.app.user.application;

import com.cdwater.cdticket.app.application.AuthService;
import com.cdwater.cdticket.app.application.RefreshTokenService;
import com.cdwater.cdticket.app.application.SmsCodeService;
import com.cdwater.cdticket.app.common.exception.BizException;
import com.cdwater.cdticket.app.infrastructure.security.JwtProperties;
import com.cdwater.cdticket.app.infrastructure.security.JwtUtil;
import com.cdwater.cdticket.app.application.dto.LoginResponse;
import com.cdwater.cdticket.app.domain.UserRepository;
import com.cdwater.cdticket.app.infrastructure.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UserRepository userRepository;
    private SmsCodeService smsCodeService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        smsCodeService = mock(SmsCodeService.class);
        JwtProperties props = new JwtProperties();
        props.setSecret("cd-ticket-dev-secret-key-0123456789abcdef0123456789abcdef");
        props.setAccessExpireSeconds(900L);
        props.setRefreshExpireSeconds(604800L);
        JwtUtil jwtUtil = new JwtUtil(props);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        when(refreshTokenService.create(anyLong())).thenReturn("refresh-token");
        authService = new AuthService(userRepository, smsCodeService, refreshTokenService,
                jwtUtil, new BCryptPasswordEncoder());
    }

    @Test
    void loginBySmsSilentlyRegisters() {
        when(userRepository.findByPhone("13800138000")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        LoginResponse resp = authService.loginBySms("13800138000", "123456");
        assertNotNull(resp.getAccessToken());
        assertNotNull(resp.getRefreshToken());
        assertEquals("用户8000", resp.getUser().getNickname());
    }

    @Test
    void loginByPasswordRejectsWrongPassword() {
        User user = new User();
        user.setId(1L);
        user.setPhone("13800138000");
        user.setPassword(new BCryptPasswordEncoder().encode("correct"));
        user.setStatus(1);
        when(userRepository.findByPhone("13800138000")).thenReturn(user);
        assertThrows(BizException.class, () -> authService.loginByPassword("13800138000", "wrong"));
    }

    @Test
    void changePasswordRejectsMismatch() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(user);
        assertThrows(BizException.class, () -> authService.changePassword(1L, "abcdef", "abcdefg"));
    }

    @Test
    void changePasswordRejectsSameAsOld() {
        User user = new User();
        user.setId(1L);
        user.setPassword(new BCryptPasswordEncoder().encode("samepass1"));
        when(userRepository.findById(1L)).thenReturn(user);
        assertThrows(BizException.class, () -> authService.changePassword(1L, "samepass1", "samepass1"));
    }
}
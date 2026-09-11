package com.cdwater.cdticket.user.application;

import com.cdwater.cdticket.common.application.BizException;
import com.cdwater.cdticket.common.application.ResultCode;
import com.cdwater.cdticket.common.infrastructure.JwtUtil;
import com.cdwater.cdticket.user.application.AuthDtos.LoginResponse;
import com.cdwater.cdticket.user.application.AuthDtos.RefreshResponse;
import com.cdwater.cdticket.user.application.AuthDtos.UserInfo;
import com.cdwater.cdticket.user.domain.UserRepository;
import com.cdwater.cdticket.user.infrastructure.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SmsCodeService smsCodeService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, SmsCodeService smsCodeService,
                       RefreshTokenService refreshTokenService, JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.smsCodeService = smsCodeService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendCode(String phone) {
        smsCodeService.sendCode(phone);
    }

    public LoginResponse loginBySms(String phone, String code) {
        smsCodeService.verify(phone, code);
        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            User u = new User();
            u.setPhone(phone);
            u.setNickname("用户" + phone.substring(phone.length() - 4));
            u.setStatus(1);
            return userRepository.save(u);
        });
        checkEnabled(user);
        return issueTokens(user);
    }

    public LoginResponse loginByPassword(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BizException(ResultCode.LOGIN_FAILED));
        checkEnabled(user);
        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        return issueTokens(user);
    }

    public RefreshResponse refresh(String refreshToken) {
        Long userId = refreshTokenService.getUserId(refreshToken);
        String newRefresh = refreshTokenService.rotate(refreshToken);
        return new RefreshResponse(jwtUtil.createUserAccessToken(userId), newRefresh);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    public void changePassword(Long userId, String newPassword, String confirmPassword) {
        if (newPassword == null || confirmPassword == null
                || newPassword.length() < 6 || newPassword.length() > 20
                || confirmPassword.length() < 6 || confirmPassword.length() > 20) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BizException(ResultCode.PASSWORD_CONFIRM_MISMATCH);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
        if (user.getPassword() != null && passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BizException(ResultCode.PASSWORD_SAME_AS_OLD);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserInfo getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
        return UserInfo.from(user);
    }

    private LoginResponse issueTokens(User user) {
        String access = jwtUtil.createUserAccessToken(user.getId());
        String refresh = refreshTokenService.create(user.getId());
        return new LoginResponse(access, refresh, UserInfo.from(user));
    }

    private void checkEnabled(User user) {
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ResultCode.USER_DISABLED);
        }
    }
}

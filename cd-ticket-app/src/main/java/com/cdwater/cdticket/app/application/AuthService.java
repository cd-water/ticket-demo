package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.common.exception.BizException;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.common.util.JwtUtil;
import com.cdwater.cdticket.app.application.dto.LoginResponse;
import com.cdwater.cdticket.app.application.dto.RefreshResponse;
import com.cdwater.cdticket.app.application.dto.UserInfo;
import com.cdwater.cdticket.app.domain.repository.UserRepository;
import com.cdwater.cdticket.app.application.convert.UserConvert;
import com.cdwater.cdticket.app.domain.model.User;
import com.cdwater.cdticket.app.interfaces.dto.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SmsCodeService smsCodeService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public void sendCode(String phone) {
        smsCodeService.sendCode(phone);
    }

    public LoginResponse loginBySms(String phone, String code) {
        smsCodeService.verify(phone, code);
        User user = userRepository.findByPhone(phone);
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickname("user_" + phone.substring(phone.length() - 4));
            user = userRepository.save(user);
        } else if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        return issueTokens(user);
    }

    public LoginResponse loginByPassword(String phone, String password) {
        User user = userRepository.findByPhone(phone);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        return issueTokens(user);
    }

    public RefreshResponse refresh(String refreshToken) {
        Long userId = refreshTokenService.getUserId(refreshToken);
        String newRefresh = refreshTokenService.rotate(refreshToken);
        RefreshResponse resp = new RefreshResponse();
        resp.setAccessToken(jwtUtil.createAccessToken(userId));
        resp.setRefreshToken(newRefresh);
        return resp;
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    public void changePassword(Long userId, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BizException(ResultCode.PASSWORD_CONFIRM_MISMATCH);
        }
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        if (user.getPassword() != null && passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BizException(ResultCode.PASSWORD_SAME_AS_OLD);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserInfo getMe(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return UserConvert.INSTANCE.toUserInfo(user);
    }

    public UserInfo updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        user.setNickname(req.getNickname());
        userRepository.save(user);
        return UserConvert.INSTANCE.toUserInfo(user);
    }

    private LoginResponse issueTokens(User user) {
        LoginResponse resp = new LoginResponse();
        resp.setAccessToken(jwtUtil.createAccessToken(user.getId()));
        resp.setRefreshToken(refreshTokenService.create(user.getId()));
        resp.setUser(UserConvert.INSTANCE.toUserInfo(user));
        return resp;
    }
}
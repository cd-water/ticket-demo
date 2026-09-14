package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.admin.LoginResponse;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.TokenStoreService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdminMapper adminMapper;
    @Mock
    private TokenStoreService tokenStoreService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private final Admin admin = buildAdmin(1L, "admin01", 1);

    private Admin buildAdmin(Long id, String username, int status) {
        Admin a = new Admin();
        a.setId(id);
        a.setUsername(username);
        a.setPassword("encoded");
        a.setStatus(status);
        return a;
    }

    private AuthService newService() {
        return new AuthService(adminMapper, tokenStoreService, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_success_returnsTokenAndAdminInfo() {
        when(adminMapper.selectOne(any())).thenReturn(admin);
        when(passwordEncoder.matches("abc12345", "encoded")).thenReturn(true);
        when(tokenStoreService.issue(1L)).thenReturn("token-1");

        LoginResponse resp = newService().login("admin01", "abc12345");

        assertThat(resp.getToken()).isEqualTo("token-1");
        assertThat(resp.getAdmin().getId()).isEqualTo(1L);
        assertThat(resp.getAdmin().getUsername()).isEqualTo("admin01");
        verify(tokenStoreService).issue(1L);
    }

    @Test
    void login_adminNotFound_throwsUnauthorized() {
        when(adminMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> newService().login("nobody", "abc12345"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(401);
    }

    @Test
    void login_adminDisabled_throwsUnauthorized() {
        Admin disabled = buildAdmin(2L, "admin02", 0);
        when(adminMapper.selectOne(any())).thenReturn(disabled);

        assertThatThrownBy(() -> newService().login("admin02", "abc12345"))
                .isInstanceOf(BizException.class)
                .hasMessage("用户名或密码错误");
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        when(adminMapper.selectOne(any())).thenReturn(admin);
        when(passwordEncoder.matches("wrongpass1", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> newService().login("admin01", "wrongpass1"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(401);
    }

    @Test
    void logout_revokesCurrentAdminToken() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(9L, null, List.of()));

        newService().logout();

        verify(tokenStoreService).revoke(9L);
    }
}

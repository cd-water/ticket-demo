package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.admin.AdminSaveRequest;
import com.cdwater.cdticket.admin.dto.admin.AdminVO;
import com.cdwater.cdticket.admin.dto.admin.ResetPasswordRequest;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.TokenStoreService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminMapper adminMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenStoreService tokenStore;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(adminMapper, passwordEncoder, tokenStore);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void list_mapsToVO() {
        Admin a = new Admin();
        a.setId(1L);
        a.setUsername("admin01");
        a.setStatus(1);
        a.setCreateTime(LocalDateTime.of(2026, 1, 1, 10, 0));
        when(adminMapper.selectList(any())).thenReturn(List.of(a));

        List<AdminVO> list = adminService.list();

        assertThat(list).hasSize(1);
        AdminVO v = list.get(0);
        assertThat(v.getId()).isEqualTo(1L);
        assertThat(v.getUsername()).isEqualTo("admin01");
        assertThat(v.getStatus()).isEqualTo(1);
        assertThat(v.getCreateTime()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    @Test
    void create_success_encodesPassword() {
        when(adminMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("abc12345")).thenReturn("hashed");

        AdminSaveRequest req = new AdminSaveRequest();
        req.setUsername("admin02");
        req.setPassword("abc12345");
        adminService.create(req);

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminMapper).insert(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("admin02");
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
    }

    @Test
    void create_duplicateUsername_throwsConflict() {
        when(adminMapper.selectOne(any())).thenReturn(new Admin());

        AdminSaveRequest req = new AdminSaveRequest();
        req.setUsername("admin01");
        req.setPassword("abc12345");

        assertThatThrownBy(() -> adminService.create(req))
                .isInstanceOf(BizException.class)
                .hasMessage("用户名已存在")
                .extracting("code")
                .isEqualTo(409);
        verify(adminMapper, never()).insert(any(Admin.class));
    }

    @Test
    void resetPassword_success_updatesAndRevokesToken() {
        Admin target = new Admin();
        target.setId(5L);
        when(adminMapper.selectById(5L)).thenReturn(target);
        when(passwordEncoder.encode("newpass123")).thenReturn("hashed2");

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setPassword("newpass123");
        adminService.resetPassword(5L, req);

        assertThat(target.getPassword()).isEqualTo("hashed2");
        verify(adminMapper).updateById(target);
        verify(tokenStore).revoke(5L);
    }

    @Test
    void resetPassword_notFound_throws404() {
        when(adminMapper.selectById(5L)).thenReturn(null);

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setPassword("newpass123");

        assertThatThrownBy(() -> adminService.resetPassword(5L, req))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    @Test
    void toggleStatus_togglingSelf_throwsForbidden() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));

        assertThatThrownBy(() -> adminService.toggleStatus(1L, 0))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(403);
        verify(adminMapper, never()).updateById(any(Admin.class));
    }

    @Test
    void toggleStatus_notFound_throws404() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
        when(adminMapper.selectById(9L)).thenReturn(null);

        assertThatThrownBy(() -> adminService.toggleStatus(9L, 0))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    @Test
    void toggleStatus_disable_revokesToken() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
        Admin target = new Admin();
        target.setId(9L);
        when(adminMapper.selectById(9L)).thenReturn(target);

        adminService.toggleStatus(9L, 0);

        assertThat(target.getStatus()).isZero();
        verify(adminMapper).updateById(target);
        verify(tokenStore).revoke(9L);
    }

    @Test
    void toggleStatus_enable_doesNotRevokeToken() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of()));
        Admin target = new Admin();
        target.setId(9L);
        when(adminMapper.selectById(9L)).thenReturn(target);

        adminService.toggleStatus(9L, 1);

        assertThat(target.getStatus()).isEqualTo(1);
        verify(adminMapper).updateById(target);
        verify(tokenStore, never()).revoke(9L);
    }
}

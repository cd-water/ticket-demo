package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.admin.AdminManageVO;
import com.cdwater.cdticket.admin.dto.admin.AdminSaveRequest;
import com.cdwater.cdticket.admin.dto.admin.ResetPasswordRequest;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import com.cdwater.cdticket.admin.security.TokenStoreService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminManageServiceTest {

    private AdminMapper mapper;
    private CinemaService cinemaService;
    private TokenStoreService tokenStore;
    private AdminManageService service;

    @BeforeEach
    void setUp() {
        mapper = mock(AdminMapper.class);
        cinemaService = mock(CinemaService.class);
        tokenStore = mock(TokenStoreService.class);
        service = new AdminManageService(mapper, cinemaService, new BCryptPasswordEncoder(), tokenStore);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authAs(long adminId, int role, long cinemaId) {
        var auth = new UsernamePasswordAuthenticationToken(adminId, null, List.of());
        auth.setDetails(new com.cdwater.cdticket.admin.security.TokenAuthenticationFilter.AdminContext(adminId, role, cinemaId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private AdminSaveRequest createReq(String username, String password, int role, Long cinemaId) {
        AdminSaveRequest req = new AdminSaveRequest();
        req.setUsername(username);
        req.setPassword(password);
        req.setRole(role);
        req.setCinemaId(cinemaId);
        return req;
    }

    private Admin admin(long id, String username, int role, long cinemaId) {
        Admin a = new Admin();
        a.setId(id);
        a.setUsername(username);
        a.setRole(role);
        a.setCinemaId(cinemaId);
        a.setStatus(1);
        return a;
    }

    @Test
    void listAsPlatformAdmin() {
        authAs(1L, 0, 0L);
        when(mapper.selectListWithCinema(any(), isNull())).thenReturn(List.of(new AdminManageVO()));
        assertEquals(1, service.list(null).size());
    }

    @Test
    void createRejectedForCinemaAdmin() {
        authAs(2L, 1, 5L);
        assertThrows(BizException.class, () -> service.create(createReq("new", "Aa123456", 0, 0L)));
    }

    @Test
    void createCinemaAdminRequiresCinema() {
        authAs(1L, 0, 0L);
        assertThrows(BizException.class, () -> service.create(createReq("new", "Aa123456", 1, null)));
    }

    @Test
    void createValidatesCinemaExists() {
        authAs(1L, 0, 0L);
        when(cinemaService.getCinema(99L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.create(createReq("new", "Aa123456", 1, 99L)));
    }

    @Test
    void createEncodesPassword() {
        authAs(1L, 0, 0L);
        service.create(createReq("new", "Aa123456", 0, 0L));
        verify(mapper).insert(ArgumentMatchers.<Admin>argThat(a -> a.getRole() == 0 && a.getCinemaId() == 0L
                && new BCryptPasswordEncoder().matches("Aa123456", a.getPassword())));
    }

    @Test
    void resetPasswordRejectedForSelf() {
        authAs(1L, 0, 0L);
        when(mapper.selectById(1L)).thenReturn(admin(1L, "admin", 0, 0L));
        assertThrows(BizException.class, () -> service.resetPassword(1L,
                new ResetPasswordRequest() {{ setPassword("NewPass99"); }}));
    }

    @Test
    void resetPasswordEncodesAndRevokesToken() {
        authAs(1L, 0, 0L);
        when(mapper.selectById(3L)).thenReturn(admin(3L, "c01", 1, 5L));
        service.resetPassword(3L, new ResetPasswordRequest() {{ setPassword("NewPass99"); }});
        verify(mapper).updateById(ArgumentMatchers.<Admin>argThat(a ->
                new BCryptPasswordEncoder().matches("NewPass99", a.getPassword())));
        verify(tokenStore).revoke(3L);
    }
}

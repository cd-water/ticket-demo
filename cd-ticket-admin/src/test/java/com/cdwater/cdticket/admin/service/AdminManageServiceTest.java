package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.admin.AdminCreateRequest;
import com.cdwater.cdticket.admin.dto.admin.AdminUpdateRequest;
import com.cdwater.cdticket.admin.entity.Admin;
import com.cdwater.cdticket.admin.mapper.AdminMapper;
import com.cdwater.cdticket.admin.security.AdminAuthorizer;
import com.cdwater.cdticket.admin.security.AdminPrincipal;
import com.cdwater.cdticket.admin.security.TokenStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminManageServiceTest {

    private AdminMapper mapper;
    private CinemaAdminService cinemaService;
    private AdminAuthorizer authorizer;
    private TokenStoreService tokenStore;
    private AdminManageService service;

    @BeforeEach
    void setUp() {
        mapper = mock(AdminMapper.class);
        cinemaService = mock(CinemaAdminService.class);
        authorizer = mock(AdminAuthorizer.class);
        tokenStore = mock(TokenStoreService.class);
        service = new AdminManageService(mapper, cinemaService, new BCryptPasswordEncoder(), authorizer, tokenStore);
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

    private AdminCreateRequest createReq(String username, String password, int role, Long cinemaId) {
        AdminCreateRequest req = new AdminCreateRequest();
        req.setUsername(username);
        req.setPassword(password);
        req.setRole(role);
        req.setCinemaId(cinemaId);
        return req;
    }

    private AdminUpdateRequest updateReq(String username, int role, Long cinemaId, int status) {
        AdminUpdateRequest req = new AdminUpdateRequest();
        req.setUsername(username);
        req.setRole(role);
        req.setCinemaId(cinemaId);
        req.setStatus(status);
        return req;
    }

    @Test
    void cinemaAdminListScopedToOwnCinema() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(2L, "cinema01", 1, 5L));
        when(mapper.selectList(any())).thenReturn(List.of(admin(3L, "c01", 1, 5L)));
        assertEquals(1, service.list(null).size());
        verify(mapper).selectList(argThat(w -> true));
    }

    @Test
    void cinemaAdminCannotCreate平台管理员() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(2L, "cinema01", 1, 5L));
        assertThrows(BizException.class, () -> service.create(createReq("new", "Aa123456", 0, 0L)));
    }

    @Test
    void 平台管理员CreatingCinemaAdminRequiresCinema() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        assertThrows(BizException.class, () -> service.create(createReq("new", "Aa123456", 1, null)));
    }

    @Test
    void 平台管理员CreatingCinemaAdminValidatesCinemaExists() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        when(cinemaService.getCinema(99L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.create(createReq("new", "Aa123456", 1, 99L)));
    }

    @Test
    void createEncodesPassword() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        service.create(createReq("new", "Aa123456", 0, 0L));
        verify(mapper).insert(ArgumentMatchers.<Admin>argThat(a -> a.getRole() == 0 && a.getCinemaId() == 0L
                && new BCryptPasswordEncoder().matches("Aa123456", a.getPassword())));
    }

    @Test
    void updateSelfRejected() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        when(mapper.selectById(1L)).thenReturn(admin(1L, "admin", 0, 0L));
        assertThrows(BizException.class, () -> service.update(1L,
                updateReq("admin", 0, 0L, 1)));
    }

    @Test
    void deleteRewritesUsernameBeforeLogicalDelete() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        when(mapper.selectById(3L)).thenReturn(admin(3L, "c01", 1, 5L));
        service.delete(3L);
        verify(mapper).updateById(ArgumentMatchers.<Admin>argThat(a -> a.getUsername().equals("c01_del_3")));
        verify(mapper).deleteById(3L);
    }
}

package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.application.dto.AdminCreateCommand;
import com.cdwater.cdticket.admin.application.dto.AdminUpdateCommand;
import com.cdwater.cdticket.admin.domain.AdminRepository;
import com.cdwater.cdticket.admin.infrastructure.entity.Admin;
import com.cdwater.cdticket.cinema.application.CinemaService;
import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.admin.AdminPrincipal;
import com.cdwater.cdticket.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminManageServiceTest {

    private AdminRepository repo;
    private CinemaService cinemaService;
    private AdminAuthorizer authorizer;
    private AdminManageService service;

    @BeforeEach
    void setUp() {
        repo = mock(AdminRepository.class);
        cinemaService = mock(CinemaService.class);
        authorizer = mock(AdminAuthorizer.class);
        service = new AdminManageService(repo, cinemaService, new BCryptPasswordEncoder(), authorizer);
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
    void cinemaAdminListScopedToOwnCinema() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(2L, "cinema01", 1, 5L));
        when(repo.list(any(), eq(5L))).thenReturn(List.of(admin(3L, "c01", 1, 5L)));
        assertEquals(1, service.list(null).size());
        verify(repo).list(any(), eq(5L));
    }

    @Test
    void cinemaAdminCannotCreateSuperAdmin() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(2L, "cinema01", 1, 5L));
        assertThrows(BizException.class, () -> service.create(new AdminCreateCommand("new", "Aa123456", 0, 0L)));
    }

    @Test
    void superAdminCreatingCinemaAdminRequiresCinema() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        assertThrows(BizException.class, () -> service.create(new AdminCreateCommand("new", "Aa123456", 1, null)));
    }

    @Test
    void superAdminCreatingCinemaAdminValidatesCinemaExists() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        when(cinemaService.getCinema(99L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.create(new AdminCreateCommand("new", "Aa123456", 1, 99L)));
    }

    @Test
    void createEncodesPassword() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        service.create(new AdminCreateCommand("new", "Aa123456", 0, 0L));
        verify(repo).save(argThat(a -> a.getRole() == 0 && a.getCinemaId() == 0L
                && new BCryptPasswordEncoder().matches("Aa123456", a.getPassword())));
    }

    @Test
    void updateSelfRejected() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        when(repo.findById(1L)).thenReturn(admin(1L, "admin", 0, 0L));
        assertThrows(BizException.class, () -> service.update(1L,
                new AdminUpdateCommand("admin", 0, 0L, 1)));
    }

    @Test
    void deleteRewritesUsernameBeforeLogicalDelete() {
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "admin", 0, 0L));
        when(repo.findById(3L)).thenReturn(admin(3L, "c01", 1, 5L));
        service.delete(3L);
        verify(repo).save(argThat(a -> a.getUsername().equals("c01_del_3")));
        verify(repo).deleteById(3L);
    }
}

package com.cdwater.cdticket.cinema.application;

import com.cdwater.cdticket.cinema.application.dto.HallSaveCommand;
import com.cdwater.cdticket.cinema.domain.HallRepository;
import com.cdwater.cdticket.cinema.infrastructure.entity.Hall;
import com.cdwater.cdticket.cinema.infrastructure.mapper.HallUsageMapper;
import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.admin.AdminPrincipal;
import com.cdwater.cdticket.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HallServiceTest {

    private HallRepository repo;
    private HallUsageMapper usageMapper;
    private AdminAuthorizer authorizer;
    private HallService service;

    @BeforeEach
    void setUp() {
        repo = mock(HallRepository.class);
        usageMapper = mock(HallUsageMapper.class);
        authorizer = mock(AdminAuthorizer.class);
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "cinema01", 1, 5L));
        service = new HallService(repo, usageMapper, authorizer);
    }

    @Test
    void listScopedToOwnCinema() {
        service.list();
        verify(repo).listByCinemaId(5L);
    }

    @Test
    void createBindsOwnCinema() {
        service.create(new HallSaveCommand("1号厅", 8, 10));
        verify(repo).save(argThat(h -> h.getCinemaId().equals(5L)));
    }

    @Test
    void deleteRejectedWhenHallHasScreenings() {
        Hall hall = new Hall(1L, 5L, "1号厅", 8, 10, 1, 0, null, null);
        when(repo.findById(1L)).thenReturn(hall);
        when(usageMapper.countByHallId(1L)).thenReturn(2L);
        assertThrows(BizException.class, () -> service.delete(1L));
    }

    @Test
    void deleteRejectedWhenScopeMismatch() {
        Hall hall = new Hall(1L, 99L, "1号厅", 8, 10, 1, 0, null, null);
        when(repo.findById(1L)).thenReturn(hall);
        doThrow(new BizException("no", "C003")).when(authorizer).requireScope(99L);
        assertThrows(BizException.class, () -> service.delete(1L));
    }
}

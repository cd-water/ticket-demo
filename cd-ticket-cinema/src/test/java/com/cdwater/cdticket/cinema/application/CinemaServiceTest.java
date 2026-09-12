package com.cdwater.cdticket.cinema.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.cinema.application.dto.CinemaVO;
import com.cdwater.cdticket.cinema.domain.CinemaRepository;
import com.cdwater.cdticket.cinema.infrastructure.entity.Cinema;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CinemaServiceTest {

    private CinemaRepository repo;
    private CinemaService service;

    @BeforeEach
    void setUp() {
        repo = mock(CinemaRepository.class);
        service = new CinemaService(repo);
    }

    @Test
    void pageFiltersByName() {
        Page<Cinema> p = new Page<>(1, 10, 1);
        p.setRecords(List.of(new Cinema(1L, "CGV影城", "成都", 1, 0, null, null)));
        when(repo.pageByName(any(), eq("CGV"))).thenReturn(p);

        PageResult<CinemaVO> r = service.page(1, 10, "CGV");

        assertEquals(1, r.getRecords().size());
        verify(repo).pageByName(any(), eq("CGV"));
    }

    @Test
    void updateMissingThrowsNotFound() {
        when(repo.findById(9L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.update(9L, null));
    }

    @Test
    void getCinemaReturnsNullWhenMissing() {
        when(repo.findById(9L)).thenReturn(null);
        assertNull(service.getCinema(9L));
    }
}

package com.cdwater.cdticket.movie.application;

import com.cdwater.cdticket.common.exception.BizException;
import com.cdwater.cdticket.movie.application.dto.BannerSaveCommand;
import com.cdwater.cdticket.movie.application.dto.BannerVO;
import com.cdwater.cdticket.movie.domain.BannerRepository;
import com.cdwater.cdticket.movie.infrastructure.entity.Banner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BannerServiceTest {

    private BannerRepository repo;
    private BannerService service;

    @BeforeEach
    void setUp() {
        repo = mock(BannerRepository.class);
        service = new BannerService(repo);
    }

    @Test
    void listReturnsOrdered() {
        when(repo.list()).thenReturn(List.of(new Banner(1L, "http://x/1.jpg", "", 1, 1, 0, null, null)));
        List<BannerVO> list = service.list();
        assertEquals(1, list.size());
        assertEquals("http://x/1.jpg", list.get(0).getImage());
    }

    @Test
    void updateMissingThrowsNotFound() {
        when(repo.findById(9L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.update(9L,
                new BannerSaveCommand("http://x/2.jpg", "", 1, 1)));
    }
}

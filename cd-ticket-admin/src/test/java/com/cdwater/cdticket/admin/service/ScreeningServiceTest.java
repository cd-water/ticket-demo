package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.entity.Screening;
import com.cdwater.cdticket.admin.mapper.ScreeningMapper;
import com.cdwater.cdticket.admin.security.TokenAuthenticationFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScreeningServiceTest {
    private ScreeningMapper screeningMapper;
    private ScreeningService screeningService;

    @BeforeEach
    void setUp() {
        screeningMapper = mock(ScreeningMapper.class);
        screeningService = new ScreeningService(screeningMapper,
                mock(MovieService.class), mock(HallService.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /** 影院管理员只能删本影院的排场 */
    @Test
    void deleteRejectsScreeningOfAnotherCinema() {
        loginAsCinemaAdmin(1L);
        when(screeningMapper.selectById(1L)).thenReturn(screeningOf(2L));

        BizException e = assertThrows(BizException.class, () -> screeningService.delete(1L));
        assertEquals(ResultCode.FORBIDDEN.getCode(), e.getCode());
        verify(screeningMapper, never()).deletePhysicallyById(any());
    }

    /** 本影院未开场的排场正常删除 */
    @Test
    void deleteRemovesOwnUpcomingScreening() {
        loginAsCinemaAdmin(1L);
        when(screeningMapper.selectById(1L)).thenReturn(screeningOf(1L));

        screeningService.delete(1L);
        verify(screeningMapper).deletePhysicallyById(1L);
    }

    /** 已开场的排场不允许删除 */
    @Test
    void deleteRejectsStartedScreening() {
        loginAsCinemaAdmin(1L);
        Screening started = screeningOf(1L);
        started.setStartTime(LocalDateTime.now().minusHours(1));
        when(screeningMapper.selectById(1L)).thenReturn(started);

        BizException e = assertThrows(BizException.class, () -> screeningService.delete(1L));
        assertEquals(ResultCode.SCREENING_STARTED.getCode(), e.getCode());
        verify(screeningMapper, never()).deletePhysicallyById(any());
    }

    private static Screening screeningOf(Long cinemaId) {
        Screening s = new Screening();
        s.setId(1L);
        s.setCinemaId(cinemaId);
        s.setStartTime(LocalDateTime.now().plusDays(1));
        return s;
    }

    /** role=1 影院管理员，SecurityUtils 从 Authentication.details 里取影院 */
    private static void loginAsCinemaAdmin(Long cinemaId) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(9L, null, List.of());
        auth.setDetails(new TokenAuthenticationFilter.AdminContext(9L, 1, cinemaId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}

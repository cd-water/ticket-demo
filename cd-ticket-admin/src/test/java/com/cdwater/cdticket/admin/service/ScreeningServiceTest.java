package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.entity.Screening;
import com.cdwater.cdticket.admin.mapper.ScreeningMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    /** 未开场的排场正常删除 */
    @Test
    void deleteRemovesUpcomingScreening() {
        when(screeningMapper.selectById(1L)).thenReturn(screeningOf());

        screeningService.delete(1L);
        verify(screeningMapper).deletePhysicallyById(1L);
    }

    /** 已开场的排场不允许删除 */
    @Test
    void deleteRejectsStartedScreening() {
        Screening started = screeningOf();
        started.setStartTime(LocalDateTime.now().minusHours(1));
        when(screeningMapper.selectById(1L)).thenReturn(started);

        BizException e = assertThrows(BizException.class, () -> screeningService.delete(1L));
        assertEquals(ResultCode.SCREENING_STARTED.getCode(), e.getCode());
        verify(screeningMapper, never()).deletePhysicallyById(org.mockito.ArgumentMatchers.any());
    }

    private static Screening screeningOf() {
        Screening s = new Screening();
        s.setId(1L);
        s.setCinemaId(1L);
        s.setStartTime(LocalDateTime.now().plusDays(1));
        return s;
    }
}

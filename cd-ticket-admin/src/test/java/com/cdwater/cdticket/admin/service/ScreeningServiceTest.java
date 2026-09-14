package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.screening.ScreeningSaveRequest;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.entity.Movie;
import com.cdwater.cdticket.admin.entity.Screening;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import com.cdwater.cdticket.admin.mapper.ScreeningMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScreeningServiceTest {
    private ScreeningMapper screeningMapper;
    private MovieMapper movieMapper;
    private HallMapper hallMapper;
    private ScreeningService screeningService;

    @BeforeEach
    void setUp() {
        screeningMapper = mock(ScreeningMapper.class);
        movieMapper = mock(MovieMapper.class);
        hallMapper = mock(HallMapper.class);
        screeningService = new ScreeningService(screeningMapper, movieMapper, hallMapper);
    }

    /** 已开场的排场不允许修改 */
    @Test
    void saveRejectsStartedScreening() {
        when(screeningMapper.selectById(1L)).thenReturn(screeningOf(LocalDateTime.now().minusHours(1)));
        stubValidMovieAndHall();

        BizException e = assertThrows(BizException.class, () -> screeningService.save(updateRequest()));
        assertEquals(ResultCode.CONFLICT.getCode(), e.getCode());
        assertEquals("排场已开场，禁止修改", e.getMessage());
    }

    /** 更新路径必须把主键带进实体，否则 updateById 的 WHERE id 为 null，静默更新 0 行 */
    @Test
    void saveUpdateCarriesId() {
        when(screeningMapper.selectById(1L)).thenReturn(screeningOf(LocalDateTime.now().plusDays(1)));
        stubValidMovieAndHall();

        screeningService.save(updateRequest());

        ArgumentCaptor<Screening> captor = ArgumentCaptor.forClass(Screening.class);
        verify(screeningMapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
    }

    private void stubValidMovieAndHall() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setStatus(1);
        when(movieMapper.selectById(1L)).thenReturn(movie);

        Hall hall = new Hall();
        hall.setId(1L);
        hall.setCinemaId(1L);
        when(hallMapper.selectById(1L)).thenReturn(hall);
    }

    private static Screening screeningOf(LocalDateTime startTime) {
        Screening s = new Screening();
        s.setId(1L);
        s.setStartTime(startTime);
        return s;
    }

    private static ScreeningSaveRequest updateRequest() {
        ScreeningSaveRequest req = new ScreeningSaveRequest();
        req.setId(1L);
        req.setMovieId(1L);
        req.setHallId(1L);
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setPrice(new BigDecimal("45.00"));
        return req;
    }
}

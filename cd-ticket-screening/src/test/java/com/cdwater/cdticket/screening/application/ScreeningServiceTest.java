package com.cdwater.cdticket.screening.application;

import com.cdwater.cdticket.cinema.application.HallService;
import com.cdwater.cdticket.cinema.application.dto.HallVO;
import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.admin.AdminPrincipal;
import com.cdwater.cdticket.common.exception.BizException;
import com.cdwater.cdticket.movie.application.MovieService;
import com.cdwater.cdticket.movie.application.dto.MovieOption;
import com.cdwater.cdticket.movie.application.dto.MovieVO;
import com.cdwater.cdticket.screening.application.dto.ScreeningSaveCommand;
import com.cdwater.cdticket.screening.domain.ScreeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScreeningServiceTest {

    private ScreeningRepository repo;
    private MovieService movieService;
    private HallService hallService;
    private AdminAuthorizer authorizer;
    private ScreeningService service;

    @BeforeEach
    void setUp() {
        repo = mock(ScreeningRepository.class);
        movieService = mock(MovieService.class);
        hallService = mock(HallService.class);
        authorizer = mock(AdminAuthorizer.class);
        when(authorizer.currentAdmin()).thenReturn(new AdminPrincipal(1L, "cinema01", 1, 5L));
        service = new ScreeningService(repo, movieService, hallService, authorizer);
    }

    @Test
    void createRejectsOffShelfMovie() {
        when(movieService.getMovie(1L)).thenReturn(movie(0));
        assertThrows(BizException.class, () -> service.create(command()));
    }

    @Test
    void createRejectsHallOfOtherCinema() {
        when(movieService.getMovie(1L)).thenReturn(movie(1));
        when(hallService.getHall(2L)).thenReturn(hall(99L));
        doThrow(new BizException("no", "C003")).when(authorizer).requireScope(99L);
        assertThrows(BizException.class, () -> service.create(command()));
    }

    @Test
    void createMapsDuplicateKeyToConflict() {
        when(movieService.getMovie(1L)).thenReturn(movie(1));
        when(hallService.getHall(2L)).thenReturn(hall(5L));
        doThrow(new DuplicateKeyException("dup")).when(repo).save(any());

        BizException e = assertThrows(BizException.class, () -> service.create(command()));
        assertEquals("C501", e.getCode());
    }

    @Test
    void pageForcesOwnCinema() {
        when(repo.pageByMovieIdAndCinemaId(any(), isNull(), eq(5L)))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));
        service.page(1, 10, null);
        verify(repo).pageByMovieIdAndCinemaId(any(), isNull(), eq(5L));
    }

    @Test
    void movieOptionsDelegatesToMovieModule() {
        MovieOption opt = new MovieOption();
        opt.setId(1L);
        opt.setTitle("星际穿越");
        when(movieService.listOptions()).thenReturn(List.of(opt));
        assertEquals(1, service.movieOptions().size());
    }

    private ScreeningSaveCommand command() {
        ScreeningSaveCommand cmd = new ScreeningSaveCommand();
        cmd.setMovieId(1L);
        cmd.setHallId(2L);
        cmd.setStartTime(LocalDateTime.now().plusDays(1));
        cmd.setPrice(new BigDecimal("45.00"));
        return cmd;
    }

    private MovieVO movie(int status) {
        MovieVO vo = new MovieVO();
        vo.setId(1L);
        vo.setTitle("星际穿越");
        vo.setStatus(status);
        return vo;
    }

    private HallVO hall(long cinemaId) {
        HallVO vo = new HallVO();
        vo.setId(2L);
        vo.setCinemaId(cinemaId);
        vo.setName("1号厅");
        vo.setSeatRows(8);
        vo.setSeatCols(10);
        return vo;
    }
}

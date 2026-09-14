package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.dto.screening.ScreeningSaveRequest;
import com.cdwater.cdticket.admin.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.entity.Movie;
import com.cdwater.cdticket.admin.entity.Screening;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import com.cdwater.cdticket.admin.mapper.ScreeningMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock
    private ScreeningMapper screeningMapper;
    @Mock
    private MovieMapper movieMapper;
    @Mock
    private HallMapper hallMapper;
    @InjectMocks
    private ScreeningService screeningService;

    private final Movie activeMovie = buildMovie(10L, 1);
    private final Hall hall = buildHall(20L, 30L);

    private Movie buildMovie(Long id, int status) {
        Movie m = new Movie();
        m.setId(id);
        m.setTitle("流浪地球3");
        m.setStatus(status);
        return m;
    }

    private Hall buildHall(Long id, Long cinemaId) {
        Hall h = new Hall();
        h.setId(id);
        h.setCinemaId(cinemaId);
        h.setName("1号厅");
        return h;
    }

    private ScreeningSaveRequest buildRequest(Long id, LocalDateTime startTime) {
        ScreeningSaveRequest req = new ScreeningSaveRequest();
        req.setId(id);
        req.setMovieId(10L);
        req.setHallId(20L);
        req.setStartTime(startTime);
        req.setPrice(new BigDecimal("45.00"));
        return req;
    }

    @Test
    void pageByCinema_returnsConvertedPage() {
        Page<ScreeningVO> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(new ScreeningVO()));
        when(screeningMapper.selectVOPage(any(), any(), any())).thenReturn(page);

        PageResult<ScreeningVO> pr = screeningService.pageByCinema(1, 10, 10L, 30L);

        assertThat(pr.getTotal()).isEqualTo(1);
        assertThat(pr.getRecords()).hasSize(1);
        verify(screeningMapper).selectVOPage(any(), org.mockito.ArgumentMatchers.eq(30L),
                org.mockito.ArgumentMatchers.eq(10L));
    }

    @Test
    void movieOptions_mapsActiveMoviesOnly() {
        Movie m = buildMovie(1L, 1);
        when(movieMapper.selectList(any())).thenReturn(List.of(m));

        List<MovieOption> options = screeningService.movieOptions();

        assertThat(options).hasSize(1);
        assertThat(options.get(0).getId()).isEqualTo(1L);
        assertThat(options.get(0).getTitle()).isEqualTo("流浪地球3");
    }

    @Test
    void save_create_success_setsCinemaFromHall() {
        when(movieMapper.selectById(10L)).thenReturn(activeMovie);
        when(hallMapper.selectById(20L)).thenReturn(hall);

        screeningService.save(buildRequest(null, LocalDateTime.now().plusHours(1)));

        ArgumentCaptor<Screening> captor = ArgumentCaptor.forClass(Screening.class);
        verify(screeningMapper).insert(captor.capture());
        Screening s = captor.getValue();
        assertThat(s.getMovieId()).isEqualTo(10L);
        assertThat(s.getHallId()).isEqualTo(20L);
        assertThat(s.getCinemaId()).isEqualTo(30L);
        assertThat(s.getPrice()).isEqualByComparingTo("45.00");
    }

    @Test
    void save_update_notFound_throws404() {
        when(screeningMapper.selectById(5L)).thenReturn(null);

        assertThatThrownBy(() -> screeningService.save(buildRequest(5L, LocalDateTime.now().plusHours(1))))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
        verify(screeningMapper, never()).updateById(any(Screening.class));
    }

    @Test
    void save_update_alreadyStarted_throwsConflict() {
        Screening existing = new Screening();
        existing.setId(5L);
        existing.setStartTime(LocalDateTime.now().minusHours(2));
        when(screeningMapper.selectById(5L)).thenReturn(existing);

        assertThatThrownBy(() -> screeningService.save(buildRequest(5L, LocalDateTime.now().plusHours(1))))
                .isInstanceOf(BizException.class)
                .hasMessage("排场已开场，禁止修改")
                .extracting("code")
                .isEqualTo(409);
    }

    @Test
    void save_update_notStarted_success() {
        Screening existing = new Screening();
        existing.setId(5L);
        existing.setStartTime(LocalDateTime.now().plusHours(2));
        when(screeningMapper.selectById(5L)).thenReturn(existing);
        when(movieMapper.selectById(10L)).thenReturn(activeMovie);
        when(hallMapper.selectById(20L)).thenReturn(hall);

        screeningService.save(buildRequest(5L, LocalDateTime.now().plusHours(3)));

        verify(screeningMapper).updateById(any(Screening.class));
    }

    @Test
    void save_movieMissing_throwsBadRequest() {
        when(movieMapper.selectById(10L)).thenReturn(null);

        assertThatThrownBy(() -> screeningService.save(buildRequest(null, LocalDateTime.now().plusHours(1))))
                .isInstanceOf(BizException.class)
                .hasMessage("电影不存在或已下架")
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void save_movieOffline_throwsBadRequest() {
        when(movieMapper.selectById(10L)).thenReturn(buildMovie(10L, 0));

        assertThatThrownBy(() -> screeningService.save(buildRequest(null, LocalDateTime.now().plusHours(1))))
                .isInstanceOf(BizException.class)
                .hasMessage("电影不存在或已下架");
    }

    @Test
    void save_hallMissing_throwsBadRequest() {
        when(movieMapper.selectById(10L)).thenReturn(activeMovie);
        when(hallMapper.selectById(20L)).thenReturn(null);

        assertThatThrownBy(() -> screeningService.save(buildRequest(null, LocalDateTime.now().plusHours(1))))
                .isInstanceOf(BizException.class)
                .hasMessage("影厅不存在")
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void save_startTimeInPast_throwsBadRequest() {
        when(movieMapper.selectById(10L)).thenReturn(activeMovie);
        when(hallMapper.selectById(20L)).thenReturn(hall);

        assertThatThrownBy(() -> screeningService.save(buildRequest(null, LocalDateTime.now().minusMinutes(5))))
                .isInstanceOf(BizException.class)
                .hasMessage("开场时间必须晚于当前时间")
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void save_duplicateKey_throwsConflict() {
        when(movieMapper.selectById(10L)).thenReturn(activeMovie);
        when(hallMapper.selectById(20L)).thenReturn(hall);
        doThrow(new DuplicateKeyException("dup"))
                .when(screeningMapper).insert(any(Screening.class));

        assertThatThrownBy(() -> screeningService.save(buildRequest(null, LocalDateTime.now().plusHours(1))))
                .isInstanceOf(BizException.class)
                .hasMessage("同影厅同一开场时间已有排场")
                .extracting("code")
                .isEqualTo(409);
    }
}

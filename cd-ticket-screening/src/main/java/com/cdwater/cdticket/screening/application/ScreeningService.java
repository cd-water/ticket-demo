package com.cdwater.cdticket.screening.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.cinema.application.HallService;
import com.cdwater.cdticket.cinema.application.dto.HallVO;
import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.api.ResultCode;
import com.cdwater.cdticket.common.exception.BizException;
import com.cdwater.cdticket.movie.application.MovieService;
import com.cdwater.cdticket.movie.application.dto.MovieOption;
import com.cdwater.cdticket.movie.application.dto.MovieVO;
import com.cdwater.cdticket.screening.application.dto.ScreeningSaveCommand;
import com.cdwater.cdticket.screening.application.dto.ScreeningVO;
import com.cdwater.cdticket.screening.domain.ScreeningRepository;
import com.cdwater.cdticket.screening.infrastructure.convert.ScreeningConvert;
import com.cdwater.cdticket.screening.infrastructure.entity.Screening;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieService movieService;
    private final HallService hallService;
    private final AdminAuthorizer adminAuthorizer;

    public PageResult<ScreeningVO> page(int page, int size, Long movieId) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        Long cinemaId = adminAuthorizer.currentAdmin().getCinemaId();
        var p = screeningRepository.pageByMovieIdAndCinemaId(Page.of(page, size), movieId, cinemaId);
        return PageResult.of(p.convert(s -> toVO(s)));
    }

    /** 排场管理电影下拉：转调 movie 模块（上架电影） */
    public List<MovieOption> movieOptions() {
        return movieService.listOptions();
    }

    public void create(ScreeningSaveCommand command) {
        save(null, command);
    }

    public void update(Long id, ScreeningSaveCommand command) {
        Screening existing = requireScreening(id);
        if (existing.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.SCREENING_STARTED);
        }
        save(id, command);
    }

    public void delete(Long id) {
        Screening existing = requireScreening(id);
        if (existing.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.SCREENING_STARTED);
        }
        screeningRepository.deleteById(id);
    }

    private void save(Long id, ScreeningSaveCommand command) {
        MovieVO movie = movieService.getMovie(command.getMovieId());
        if (movie == null || movie.getStatus() != 1) {
            throw new BizException("电影不存在或已下架", ResultCode.BAD_REQUEST.getCode());
        }
        HallVO hall = hallService.getHall(command.getHallId());
        if (hall == null) {
            throw new BizException("影厅不存在", ResultCode.BAD_REQUEST.getCode());
        }
        adminAuthorizer.requireScope(hall.getCinemaId());
        if (!command.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BizException("开场时间必须晚于当前时间", ResultCode.BAD_REQUEST.getCode());
        }
        Screening s = ScreeningConvert.INSTANCE.toEntity(command);
        if (id != null) {
            s.setId(id);
        }
        s.setCinemaId(hall.getCinemaId());
        try {
            screeningRepository.save(s);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.SCREENING_TIME_CONFLICT);
        }
    }

    private ScreeningVO toVO(Screening s) {
        MovieVO movie = movieService.getMovie(s.getMovieId());
        HallVO hall = hallService.getHall(s.getHallId());
        ScreeningVO vo = new ScreeningVO();
        vo.setId(s.getId());
        vo.setMovieId(s.getMovieId());
        vo.setMovieTitle(movie == null ? "" : movie.getTitle());
        vo.setHallId(s.getHallId());
        vo.setHallName(hall == null ? "" : hall.getName());
        vo.setCinemaId(s.getCinemaId());
        vo.setStartTime(s.getStartTime());
        vo.setPrice(s.getPrice());
        vo.setStatus(s.getStartTime().isBefore(LocalDateTime.now()) ? 1 : 0);
        return vo;
    }

    private Screening requireScreening(Long id) {
        Screening s = screeningRepository.findById(id);
        if (s == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return s;
    }
}

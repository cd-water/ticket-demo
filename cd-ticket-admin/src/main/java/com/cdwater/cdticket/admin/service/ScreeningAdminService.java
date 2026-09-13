package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.convert.ScreeningConvert;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.dto.screening.ScreeningSaveRequest;
import com.cdwater.cdticket.admin.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.entity.Screening;
import com.cdwater.cdticket.admin.mapper.ScreeningMapper;
import com.cdwater.cdticket.admin.security.AdminAuthorizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreeningAdminService {

    private final ScreeningMapper screeningMapper;
    private final MovieAdminService movieAdminService;
    private final HallAdminService hallAdminService;
    private final AdminAuthorizer adminAuthorizer;

    public PageResult<ScreeningVO> page(int page, int size, Long movieId) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        Long cinemaId = adminAuthorizer.currentAdmin().getCinemaId();
        var p = screeningMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Screening>()
                .eq(movieId != null, Screening::getMovieId, movieId)
                .eq(Screening::getCinemaId, cinemaId)
                .orderByDesc(Screening::getStartTime));
        return PageResult.of(p.convert(s -> toVO(s)));
    }

    /** 排场管理电影下拉：转调 movie 模块（上架电影） */
    public List<MovieOption> movieOptions() {
        return movieAdminService.listOptions();
    }

    public void create(ScreeningSaveRequest req) {
        save(null, req);
    }

    public void update(Long id, ScreeningSaveRequest req) {
        Screening existing = requireScreening(id);
        if (existing.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.SCREENING_STARTED);
        }
        save(id, req);
    }

    public void delete(Long id) {
        Screening existing = requireScreening(id);
        if (existing.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.SCREENING_STARTED);
        }
        screeningMapper.deleteById(id);
    }

    private void save(Long id, ScreeningSaveRequest req) {
        MovieVO movie = movieAdminService.getMovie(req.getMovieId());
        if (movie == null || movie.getStatus() != 1) {
            throw new BizException("电影不存在或已下架", ResultCode.BAD_REQUEST.getCode());
        }
        HallVO hall = hallAdminService.getHall(req.getHallId());
        if (hall == null) {
            throw new BizException("影厅不存在", ResultCode.BAD_REQUEST.getCode());
        }
        adminAuthorizer.requireScope(hall.getCinemaId());
        if (!req.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BizException("开场时间必须晚于当前时间", ResultCode.BAD_REQUEST.getCode());
        }
        Screening s = ScreeningConvert.INSTANCE.toEntity(req);
        if (id != null) {
            s.setId(id);
        }
        s.setCinemaId(hall.getCinemaId());
        try {
            if (id == null) {
                screeningMapper.insert(s);
            } else {
                screeningMapper.updateById(s);
            }
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.SCREENING_TIME_CONFLICT);
        }
    }

    private ScreeningVO toVO(Screening s) {
        MovieVO movie = movieAdminService.getMovie(s.getMovieId());
        HallVO hall = hallAdminService.getHall(s.getHallId());
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
        Screening s = screeningMapper.selectById(id);
        if (s == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return s;
    }
}

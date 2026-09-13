package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.dto.screening.ScreeningSaveRequest;
import com.cdwater.cdticket.admin.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.entity.Screening;
import com.cdwater.cdticket.admin.mapper.ScreeningMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningMapper screeningMapper;
    private final MovieService movieAdminService;
    private final HallService hallAdminService;

    public PageResult<ScreeningVO> page(int page, int size, Long movieId) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        Long cinemaId = SecurityUtils.isPlatformAdmin() ? null : SecurityUtils.getCinemaId();
        var p = screeningMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Screening>()
                .eq(movieId != null, Screening::getMovieId, movieId)
                .eq(cinemaId != null, Screening::getCinemaId, cinemaId)
                .orderByDesc(Screening::getStartTime));
        return PageResult.of(p.convert(s -> toVO(s)));
    }

    public List<MovieOption> movieOptions() {
        return movieAdminService.listOptions();
    }

    /** 新增/修改（id=null → 新增） */
    public void save(ScreeningSaveRequest req) {
        boolean isCreate = req.getId() == null;
        if (!isCreate) {
            Screening existing = requireScreening(req.getId());
            if (existing.getStartTime().isBefore(LocalDateTime.now())) {
                throw new BizException(ResultCode.SCREENING_STARTED);
            }
        }
        MovieVO movie = movieAdminService.getMovie(req.getMovieId());
        if (movie == null || movie.getStatus() != 1) {
            throw new BizException("电影不存在或已下架", ResultCode.BAD_REQUEST.getCode());
        }
        HallVO hall = hallAdminService.getHall(req.getHallId());
        if (hall == null) {
            throw new BizException("影厅不存在", ResultCode.BAD_REQUEST.getCode());
        }
        SecurityUtils.requireScope(hall.getCinemaId());
        if (!req.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BizException("开场时间必须晚于当前时间", ResultCode.BAD_REQUEST.getCode());
        }
        Screening s = toEntity(req);
        s.setCinemaId(hall.getCinemaId());
        try {
            if (isCreate) {
                screeningMapper.insert(s);
            } else {
                screeningMapper.updateById(s);
            }
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.SCREENING_TIME_CONFLICT);
        }
    }

    public void delete(Long id) {
        Screening existing = requireScreening(id);
        if (existing.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.SCREENING_STARTED);
        }
        screeningMapper.deleteById(id);
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
        if (s == null) throw new BizException(ResultCode.NOT_FOUND);
        return s;
    }

    private static Screening toEntity(ScreeningSaveRequest req) {
        Screening s = new Screening();
        s.setMovieId(req.getMovieId());
        s.setHallId(req.getHallId());
        s.setStartTime(req.getStartTime());
        s.setPrice(req.getPrice());
        return s;
    }
}

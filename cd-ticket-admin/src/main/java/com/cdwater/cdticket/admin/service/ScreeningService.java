package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.dto.screening.ScreeningSaveRequest;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.entity.Movie;
import com.cdwater.cdticket.admin.entity.Screening;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import com.cdwater.cdticket.admin.mapper.ScreeningMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreeningService {
    private final ScreeningMapper screeningMapper;
    private final MovieMapper movieMapper;
    private final HallMapper hallMapper;

    public PageResult<ScreeningVO> pageByCinema(int page, int size, Long movieId, Long cinemaId) {
        IPage<ScreeningVO> p = screeningMapper.selectPage(Page.of(page, size), cinemaId, movieId);
        return PageResult.of(p);
    }

    public List<MovieOption> movieOptions() {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, 1)
                .orderByAsc(Movie::getId)).stream().map(ScreeningService::toOption).toList();
    }

    public void save(ScreeningSaveRequest req) {
        if (req.getId() != null) {
            Screening existing = screeningMapper.selectById(req.getId());
            if (existing == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            if (existing.getStartTime().isBefore(LocalDateTime.now())) {
                throw new BizException("排场已开场，禁止修改", ResultCode.CONFLICT.getCode());
            }
        }
        Movie movie = movieMapper.selectById(req.getMovieId());
        if (movie == null || movie.getStatus() != 1) {
            throw new BizException("电影不存在或已下架", ResultCode.BAD_REQUEST.getCode());
        }
        Hall hall = hallMapper.selectById(req.getHallId());
        if (hall == null) {
            throw new BizException("影厅不存在", ResultCode.BAD_REQUEST.getCode());
        }
        if (!req.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BizException("开场时间必须晚于当前时间", ResultCode.BAD_REQUEST.getCode());
        }
        Screening s = toEntity(req);
        s.setCinemaId(hall.getCinemaId());
        try {
            if (req.getId() == null) {
                screeningMapper.insert(s);
            } else {
                screeningMapper.updateById(s);
            }
        } catch (DuplicateKeyException e) {
            throw new BizException("同影厅同一开场时间已有排场", ResultCode.CONFLICT.getCode());
        }
    }

    private static Screening toEntity(ScreeningSaveRequest req) {
        Screening s = new Screening();
        s.setId(req.getId());
        s.setMovieId(req.getMovieId());
        s.setHallId(req.getHallId());
        s.setStartTime(req.getStartTime());
        s.setPrice(req.getPrice());
        return s;
    }

    private static MovieOption toOption(Movie m) {
        MovieOption o = new MovieOption();
        o.setId(m.getId());
        o.setTitle(m.getTitle());
        return o;
    }
}

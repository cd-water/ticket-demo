package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.dto.screening.ScreeningSaveRequest;
import com.cdwater.cdticket.admin.entity.Screening;
import com.cdwater.cdticket.admin.mapper.ScreeningMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScreeningService {
    private final ScreeningMapper screeningMapper;
    private final MovieService movieService;
    private final HallService hallService;

    public PageResult<ScreeningVO> page(int page, int size, Long movieId) {
        PageResult.check(page, size);
        IPage<Screening> p = screeningMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Screening>()
                .eq(movieId != null, Screening::getMovieId, movieId)
                .eq(Screening::getCinemaId, SecurityUtils.getCinemaId())
                .orderByDesc(Screening::getStartTime));

        Set<Long> movieIds = p.getRecords().stream().map(Screening::getMovieId).collect(Collectors.toSet());
        Set<Long> hallIds = p.getRecords().stream().map(Screening::getHallId).collect(Collectors.toSet());
        Map<Long, String> movieTitles = movieService.mapTitlesByIds(movieIds);
        Map<Long, String> hallNames = hallService.mapNamesByIds(hallIds);
        return PageResult.of(p.convert(s -> toVO(s, movieTitles, hallNames)));
    }

    public List<MovieOption> movieOptions() {
        return movieService.listOptions();
    }

    public void save(ScreeningSaveRequest req) {
        boolean isCreate = req.getId() == null;
        if (!isCreate) {
            Screening existing = requireScreening(req.getId());
            SecurityUtils.requireScope(existing.getCinemaId());
            if (existing.getStartTime().isBefore(LocalDateTime.now())) {
                throw new BizException(ResultCode.SCREENING_STARTED);
            }
        }
        MovieVO movie = movieService.getMovie(req.getMovieId());
        if (movie == null || movie.getStatus() != 1) {
            throw new BizException("电影不存在或已下架", ResultCode.BAD_REQUEST.getCode());
        }
        HallVO hall = hallService.getHall(req.getHallId());
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
        SecurityUtils.requireScope(existing.getCinemaId());
        if (existing.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.SCREENING_STARTED);
        }
        screeningMapper.deletePhysicallyById(id);
    }

    private ScreeningVO toVO(Screening s, Map<Long, String> movieTitles, Map<Long, String> hallNames) {
        ScreeningVO vo = new ScreeningVO();
        vo.setId(s.getId());
        vo.setMovieId(s.getMovieId());
        vo.setMovieTitle(movieTitles.getOrDefault(s.getMovieId(), ""));
        vo.setHallId(s.getHallId());
        vo.setHallName(hallNames.getOrDefault(s.getHallId(), ""));
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

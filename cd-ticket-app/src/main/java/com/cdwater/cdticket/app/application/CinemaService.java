package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.CinemaDetailVO;
import com.cdwater.cdticket.app.application.dto.CinemaVO;
import com.cdwater.cdticket.app.application.dto.MovieVO;
import com.cdwater.cdticket.app.application.dto.ScreeningVO;
import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.common.exception.BizException;
import com.cdwater.cdticket.app.domain.model.Cinema;
import com.cdwater.cdticket.app.domain.model.Movie;
import com.cdwater.cdticket.app.domain.model.Screening;
import com.cdwater.cdticket.app.domain.repository.CinemaRepository;
import com.cdwater.cdticket.app.domain.repository.MovieRepository;
import com.cdwater.cdticket.app.infrastructure.service.CacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private static final long LIST_CACHE_TTL = 120;

    private final CinemaRepository cinemaRepository;
    private final MovieRepository movieRepository;
    private final CacheService cacheService;

    private static CinemaVO toVO(Cinema c) {
        CinemaVO vo = new CinemaVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setAddress(c.getAddress());
        vo.setStatus(c.getStatus());
        return vo;
    }

    /** 影院分页（停业/营业都返回，按 id 升序）；逻辑过期缓存 */
    public PageResult<CinemaVO> page(int page, int size) {
        return cacheService.getOrLoad("cinemas:" + page + ":" + size, LIST_CACHE_TTL, () -> {
            PageResult<Cinema> p = cinemaRepository.page(page, size);
            return new PageResult<>(p.getTotal(), p.getRecords().stream().map(CinemaService::toVO).toList(),
                    p.getPage(), p.getSize());
        }, new TypeReference<>() {});
    }

    /** 影院详情；不存在抛 NOT_FOUND */
    public CinemaDetailVO detail(Long id) {
        Cinema c = cinemaRepository.findById(id);
        if (c == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        CinemaDetailVO vo = new CinemaDetailVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setAddress(c.getAddress());
        vo.setStatus(c.getStatus());
        vo.setMovies(cinemaRepository.findMoviesByCinema(c.getId()).stream()
                .map(m -> {
                    MovieVO mv = new MovieVO();
                    mv.setId(m.getId());
                    mv.setTitle(m.getTitle());
                    mv.setPoster(m.getPoster());
                    return mv;
                }).toList());
        return vo;
    }

    /** 影院排片列表（可按 movieId 过滤） */
    public List<ScreeningVO> screenings(Long cinemaId, Long movieId) {
        if (cinemaRepository.findById(cinemaId) == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        List<Screening> list = cinemaRepository.findScreenings(cinemaId, movieId);
        if (list.isEmpty()) {
            return List.of();
        }

        Map<Long, String> hallNames = cinemaRepository.findHallNames(
                list.stream().map(Screening::getHallId).distinct().toList());
        Map<Long, Integer> durations = movieRepository.findByIds(
                        list.stream().map(Screening::getMovieId).distinct().toList())
                .stream().collect(Collectors.toMap(Movie::getId, Movie::getDuration));

        return list.stream().map(s -> {
            ScreeningVO vo = new ScreeningVO();
            vo.setId(s.getId());
            vo.setMovieId(s.getMovieId());
            vo.setHallId(s.getHallId());
            vo.setHallName(hallNames.get(s.getHallId()));
            vo.setStartTime(s.getStartTime());
            vo.setEndTime(s.getStartTime().plusMinutes(durations.get(s.getMovieId())));
            vo.setPrice(s.getPrice());
            return vo;
        }).toList();
    }
}

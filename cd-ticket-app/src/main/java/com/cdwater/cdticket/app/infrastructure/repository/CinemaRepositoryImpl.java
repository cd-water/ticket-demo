package com.cdwater.cdticket.app.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.domain.model.Cinema;
import com.cdwater.cdticket.app.domain.model.Hall;
import com.cdwater.cdticket.app.domain.model.Movie;
import com.cdwater.cdticket.app.domain.model.Screening;
import com.cdwater.cdticket.app.domain.repository.CinemaRepository;
import com.cdwater.cdticket.app.infrastructure.mapper.CinemaMapper;
import com.cdwater.cdticket.app.infrastructure.mapper.HallMapper;
import com.cdwater.cdticket.app.infrastructure.mapper.MovieMapper;
import com.cdwater.cdticket.app.infrastructure.mapper.ScreeningMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CinemaRepositoryImpl implements CinemaRepository {

    private final CinemaMapper cinemaMapper;
    private final ScreeningMapper screeningMapper;
    private final HallMapper hallMapper;
    private final MovieMapper movieMapper;

    @Override
    public PageResult<Cinema> page(int page, int size) {
        IPage<Cinema> p = cinemaMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<Cinema>().orderByAsc(Cinema::getId));
        return PageResult.of(p);
    }

    @Override
    public Cinema findById(Long id) {
        return cinemaMapper.selectById(id);
    }

    @Override
    public List<Screening> findScreenings(Long cinemaId, Long movieId) {
        return screeningMapper.selectList(new LambdaQueryWrapper<Screening>()
                .eq(Screening::getCinemaId, cinemaId)
                .eq(movieId != null, Screening::getMovieId, movieId)
                .orderByAsc(Screening::getStartTime));
    }

    @Override
    public List<Movie> findMoviesByCinema(Long cinemaId) {
        List<Long> movieIds = screeningMapper.selectList(new LambdaQueryWrapper<Screening>()
                        .eq(Screening::getCinemaId, cinemaId)
                        .select(Screening::getMovieId))
                .stream().map(Screening::getMovieId).distinct().toList();
        if (movieIds.isEmpty()) {
            return List.of();
        }
        return movieMapper.selectBatchIds(movieIds).stream()
                .sorted((a, b) -> movieIds.indexOf(a.getId()) - movieIds.indexOf(b.getId()))
                .toList();
    }

    @Override
    public Map<Long, String> findHallNames(Collection<Long> hallIds) {
        return hallMapper.selectBatchIds(hallIds).stream()
                .collect(Collectors.toMap(Hall::getId, Hall::getName));
    }
}

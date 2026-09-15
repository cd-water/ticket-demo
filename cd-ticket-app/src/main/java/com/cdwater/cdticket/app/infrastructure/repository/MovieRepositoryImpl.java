package com.cdwater.cdticket.app.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.app.application.dto.BoxOfficeVO;
import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.domain.model.Movie;
import com.cdwater.cdticket.app.domain.repository.MovieRepository;
import com.cdwater.cdticket.app.infrastructure.mapper.MovieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryImpl implements MovieRepository {

    private final MovieMapper movieMapper;

    @Override
    public List<Movie> findHot(int limit) {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, 1)
                .le(Movie::getReleaseDate, LocalDate.now())
                .orderByDesc(Movie::getReleaseDate)
                .last("LIMIT " + limit));
    }

    @Override
    public List<Movie> findComing(int limit) {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, 1)
                .gt(Movie::getReleaseDate, LocalDate.now())
                .orderByAsc(Movie::getReleaseDate)
                .last("LIMIT " + limit));
    }

    @Override
    public PageResult<Movie> page(String showStatus, int page, int size) {
        IPage<Movie> p = movieMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, 1)
                .le("hot".equals(showStatus), Movie::getReleaseDate, LocalDate.now())
                .gt("coming".equals(showStatus), Movie::getReleaseDate, LocalDate.now())
                .orderByDesc("hot".equals(showStatus), Movie::getReleaseDate)
                .orderByAsc("coming".equals(showStatus), Movie::getReleaseDate));
        return PageResult.of(p);
    }

    @Override
    public Movie findById(Long id) {
        return movieMapper.selectById(id);
    }

    @Override
    public List<Movie> findByIds(Collection<Long> ids) {
        return movieMapper.selectBatchIds(ids);
    }

    @Override
    public List<BoxOfficeVO> boxOffice() {
        return movieMapper.selectBoxOffice();
    }
}

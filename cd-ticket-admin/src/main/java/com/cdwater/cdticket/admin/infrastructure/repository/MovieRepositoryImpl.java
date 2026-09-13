package com.cdwater.cdticket.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.domain.MovieRepository;
import com.cdwater.cdticket.admin.infrastructure.entity.Movie;
import com.cdwater.cdticket.admin.infrastructure.mapper.MovieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MovieRepositoryImpl implements MovieRepository {

    private final MovieMapper movieMapper;

    @Override
    public Movie findById(Long id) {
        return movieMapper.selectById(id);
    }

    @Override
    public IPage<Movie> pageByTitleAndStatus(Page<Movie> page, String title, Integer status) {
        return movieMapper.selectPage(page, new LambdaQueryWrapper<Movie>()
                .like(title != null && !title.isBlank(), Movie::getTitle, title)
                .eq(status != null, Movie::getStatus, status)
                .orderByDesc(Movie::getCreateTime));
    }

    @Override
    public List<Movie> listOptions() {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, 1)
                .orderByAsc(Movie::getId));
    }

    @Override
    public Movie save(Movie movie) {
        if (movie.getId() == null) {
            movieMapper.insert(movie);
        } else {
            movieMapper.updateById(movie);
        }
        return movie;
    }

    @Override
    public void deleteById(Long id) {
        movieMapper.deleteById(id);
    }

    @Override
    public void updateAllColumns(Long id, Movie movie) {
        // poster 列为 NOT NULL DEFAULT ''，置空须写空串（null 会违反约束）
        String poster = movie.getPoster() == null ? "" : movie.getPoster();
        movieMapper.update(null, new LambdaUpdateWrapper<Movie>()
                .eq(Movie::getId, id)
                .set(Movie::getTitle, movie.getTitle())
                .set(Movie::getPoster, poster)
                .set(Movie::getDescription, movie.getDescription())
                .set(Movie::getDuration, movie.getDuration())
                .set(Movie::getReleaseDate, movie.getReleaseDate())
                .set(Movie::getStatus, movie.getStatus()));
    }
}

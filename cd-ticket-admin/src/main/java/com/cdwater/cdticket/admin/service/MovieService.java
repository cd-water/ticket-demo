package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.dto.movie.MovieSaveRequest;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.entity.Movie;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieMapper movieMapper;

    public PageResult<MovieVO> page(int page, int size, String title, Integer status) {
        IPage<Movie> p = movieMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Movie>()
                .like(title != null && !title.isBlank(), Movie::getTitle, title)
                .eq(status != null, Movie::getStatus, status)
                .orderByDesc(Movie::getCreateTime));
        return PageResult.of(p.convert(MovieService::toVO));
    }

    public void save(MovieSaveRequest req) {
        if (req.getId() == null) {
            movieMapper.insert(toEntity(req));
            return;
        }
        requireMovie(req.getId());
        String poster = req.getPoster() == null ? "" : req.getPoster();
        String description = req.getDescription() == null ? "" : req.getDescription();
        movieMapper.update(null, new LambdaUpdateWrapper<Movie>()
                .eq(Movie::getId, req.getId())
                .set(Movie::getTitle, req.getTitle())
                .set(Movie::getPoster, poster)
                .set(Movie::getDescription, description)
                .set(Movie::getDuration, req.getDuration())
                .set(Movie::getReleaseDate, req.getReleaseDate())
                .set(Movie::getStatus, req.getStatus()));
    }

    public void delete(Long id) {
        requireMovie(id);
        movieMapper.deleteById(id);
    }

    public MovieVO getMovie(Long id) {
        Movie movie = movieMapper.selectById(id);
        return movie == null ? null : toVO(movie);
    }

    public Map<Long, String> mapTitlesByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return movieMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Movie::getId, Movie::getTitle));
    }

    public List<MovieOption> listOptions() {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, 1)
                .orderByAsc(Movie::getId)).stream().map(MovieService::toOption).toList();
    }

    private Movie requireMovie(Long id) {
        Movie movie = movieMapper.selectById(id);
        if (movie == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return movie;
    }

    private static MovieVO toVO(Movie m) {
        MovieVO v = new MovieVO();
        v.setId(m.getId());
        v.setTitle(m.getTitle());
        v.setPoster(m.getPoster());
        v.setDescription(m.getDescription());
        v.setDuration(m.getDuration());
        v.setReleaseDate(m.getReleaseDate());
        v.setStatus(m.getStatus());
        v.setCreateTime(m.getCreateTime());
        v.setUpdateTime(m.getUpdateTime());
        return v;
    }

    private static Movie toEntity(MovieSaveRequest req) {
        Movie m = new Movie();
        m.setTitle(req.getTitle());
        m.setPoster(req.getPoster() == null ? "" : req.getPoster());
        m.setDescription(req.getDescription());
        m.setDuration(req.getDuration());
        m.setReleaseDate(req.getReleaseDate());
        m.setStatus(req.getStatus());
        return m;
    }

    private static MovieOption toOption(Movie m) {
        MovieOption o = new MovieOption();
        o.setId(m.getId());
        o.setTitle(m.getTitle());
        return o;
    }
}

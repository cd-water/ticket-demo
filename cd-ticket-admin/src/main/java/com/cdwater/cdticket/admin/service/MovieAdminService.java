package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.convert.MovieConvert;
import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.dto.movie.MovieSaveRequest;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.entity.Movie;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieAdminService {

    private final MovieMapper movieMapper;

    public PageResult<MovieVO> page(int page, int size, String title, Integer status) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        var p = movieMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Movie>()
                .like(title != null && !title.isBlank(), Movie::getTitle, title)
                .eq(status != null, Movie::getStatus, status)
                .orderByDesc(Movie::getCreateTime));
        return PageResult.of(p.convert(MovieConvert.INSTANCE::toVO));
    }

    public void create(MovieSaveRequest req) {
        movieMapper.insert(MovieConvert.INSTANCE.toEntity(req));
    }

    public void update(Long id, MovieSaveRequest req) {
        requireMovie(id);
        // poster 列为 NOT NULL DEFAULT ''，置空须写空串（null 会违反约束）
        String poster = req.getPoster() == null ? "" : req.getPoster();
        movieMapper.update(null, new LambdaUpdateWrapper<Movie>()
                .eq(Movie::getId, id)
                .set(Movie::getTitle, req.getTitle())
                .set(Movie::getPoster, poster)
                .set(Movie::getDescription, req.getDescription())
                .set(Movie::getDuration, req.getDuration())
                .set(Movie::getReleaseDate, req.getReleaseDate())
                .set(Movie::getStatus, req.getStatus()));
    }

    public void delete(Long id) {
        requireMovie(id);
        movieMapper.deleteById(id);
    }

    /** 供 screening 模块校验：不存在返回 null */
    public MovieVO getMovie(Long id) {
        Movie movie = movieMapper.selectById(id);
        return movie == null ? null : MovieConvert.INSTANCE.toVO(movie);
    }

    /** 上架电影下拉（供排场管理） */
    public List<MovieOption> listOptions() {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getStatus, 1)
                .orderByAsc(Movie::getId)).stream().map(MovieConvert.INSTANCE::toOption).toList();
    }

    private Movie requireMovie(Long id) {
        Movie movie = movieMapper.selectById(id);
        if (movie == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return movie;
    }
}

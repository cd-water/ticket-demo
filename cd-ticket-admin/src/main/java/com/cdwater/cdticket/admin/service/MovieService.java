package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.movie.MovieSaveRequest;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.entity.Movie;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieMapper movieMapper;

    public PageResult<MovieVO> page(int page, int size, String title, Integer status) {
        IPage<Movie> p = movieMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Movie>()
                .like(title != null && !title.isBlank(), Movie::getTitle, title)
                .eq(status != null, Movie::getStatus, status)
                .orderByDesc(Movie::getId));
        return PageResult.of(p.convert(MovieService::toVO));
    }

    public void save(MovieSaveRequest req) {
        Movie movie = toEntity(req);
        if (req.getId() == null) {
            movieMapper.insert(movie);
        } else {
            if (movieMapper.selectById(req.getId()) == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            movieMapper.updateById(movie);
        }
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
        m.setId(req.getId());
        m.setTitle(req.getTitle());
        m.setPoster(req.getPoster());
        m.setDescription(req.getDescription());
        m.setDuration(req.getDuration());
        m.setReleaseDate(req.getReleaseDate());
        m.setStatus(req.getStatus());
        return m;
    }
}

package com.cdwater.cdticket.admin.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.application.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.application.dto.movie.MovieSaveCommand;
import com.cdwater.cdticket.admin.application.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.application.dto.PageResult;
import com.cdwater.cdticket.admin.application.dto.ResultCode;
import com.cdwater.cdticket.admin.application.exception.BizException;
import com.cdwater.cdticket.admin.domain.MovieRepository;
import com.cdwater.cdticket.admin.infrastructure.convert.MovieConvert;
import com.cdwater.cdticket.admin.infrastructure.entity.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieAdminService {

    private final MovieRepository movieRepository;

    public PageResult<MovieVO> page(int page, int size, String title, Integer status) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        var p = movieRepository.pageByTitleAndStatus(Page.of(page, size), title, status);
        return PageResult.of(p.convert(MovieConvert.INSTANCE::toVO));
    }

    public void create(MovieSaveCommand command) {
        movieRepository.save(MovieConvert.INSTANCE.toEntity(command));
    }

    public void update(Long id, MovieSaveCommand command) {
        requireMovie(id);
        movieRepository.updateAllColumns(id, MovieConvert.INSTANCE.toEntity(command));
    }

    public void delete(Long id) {
        requireMovie(id);
        movieRepository.deleteById(id);
    }

    /** 供 screening 模块校验：不存在返回 null */
    public MovieVO getMovie(Long id) {
        Movie movie = movieRepository.findById(id);
        return movie == null ? null : MovieConvert.INSTANCE.toVO(movie);
    }

    /** 上架电影下拉（供排场管理） */
    public List<MovieOption> listOptions() {
        return movieRepository.listOptions().stream().map(MovieConvert.INSTANCE::toOption).toList();
    }

    private Movie requireMovie(Long id) {
        Movie movie = movieRepository.findById(id);
        if (movie == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return movie;
    }
}

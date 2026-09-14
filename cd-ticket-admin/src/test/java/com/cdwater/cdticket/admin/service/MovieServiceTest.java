package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.movie.MovieSaveRequest;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.entity.Movie;
import com.cdwater.cdticket.admin.mapper.MovieMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieMapper movieMapper;
    @InjectMocks
    private MovieService movieService;

    private MovieSaveRequest buildRequest(Long id) {
        MovieSaveRequest req = new MovieSaveRequest();
        req.setId(id);
        req.setTitle("流浪地球3");
        req.setPoster("poster.png");
        req.setDescription("描述");
        req.setDuration(120);
        req.setReleaseDate(LocalDate.of(2026, 10, 1));
        req.setStatus(1);
        return req;
    }

    @Test
    void page_returnsConvertedPage() {
        Movie m = new Movie();
        m.setId(1L);
        m.setTitle("流浪地球3");
        m.setStatus(1);
        Page<Movie> moviePage = new Page<>(1, 10);
        moviePage.setTotal(1);
        moviePage.setRecords(List.of(m));
        when(movieMapper.selectPage(any(), any())).thenReturn(moviePage);

        PageResult<MovieVO> pr = movieService.page(1, 10, "流浪", 1);

        assertThat(pr.getTotal()).isEqualTo(1);
        assertThat(pr.getRecords()).hasSize(1);
        assertThat(pr.getRecords().get(0).getTitle()).isEqualTo("流浪地球3");
    }

    @Test
    void save_create_inserts() {
        movieService.save(buildRequest(null));
        verify(movieMapper).insert(any(Movie.class));
        verify(movieMapper, never()).updateById(any(Movie.class));
    }

    @Test
    void save_update_updatesExisting() {
        when(movieMapper.selectById(5L)).thenReturn(new Movie());
        movieService.save(buildRequest(5L));
        verify(movieMapper).updateById(any(Movie.class));
        verify(movieMapper, never()).insert(any(Movie.class));
    }

    @Test
    void save_update_notFound_throws404() {
        when(movieMapper.selectById(5L)).thenReturn(null);

        assertThatThrownBy(() -> movieService.save(buildRequest(5L)))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
        verify(movieMapper, never()).updateById(any(Movie.class));
    }
}

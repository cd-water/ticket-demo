package com.cdwater.cdticket.movie.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.exception.BizException;
import com.cdwater.cdticket.movie.application.dto.MovieOption;
import com.cdwater.cdticket.movie.application.dto.MovieSaveCommand;
import com.cdwater.cdticket.movie.application.dto.MovieVO;
import com.cdwater.cdticket.movie.domain.MovieRepository;
import com.cdwater.cdticket.movie.infrastructure.entity.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    private MovieRepository repo;
    private MovieService service;

    @BeforeEach
    void setUp() {
        repo = mock(MovieRepository.class);
        service = new MovieService(repo);
    }

    @Test
    void pagePassesFiltersToRepository() {
        Page<Movie> p = new Page<>(1, 10, 1);
        p.setRecords(List.of(movie()));
        when(repo.pageByTitleAndStatus(any(), eq("星际"), eq(1))).thenReturn(p);

        PageResult<MovieVO> r = service.page(1, 10, "星际", 1);

        assertEquals(1, r.getRecords().size());
        assertEquals("星际穿越", r.getRecords().get(0).getTitle());
        verify(repo).pageByTitleAndStatus(any(), eq("星际"), eq(1));
    }

    @Test
    void updateMissingThrowsNotFound() {
        when(repo.findById(99L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.update(99L, command()));
    }

    @Test
    void deleteMissingThrowsNotFound() {
        when(repo.findById(99L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.delete(99L));
    }

    @Test
    void listOptionsOnlyOnShelf() {
        when(repo.listOptions()).thenReturn(List.of(new Movie(1L, "星际穿越", "", null, 169,
                LocalDate.of(2026, 9, 1), 1, 0, null, null)));
        List<MovieOption> opts = service.listOptions();
        assertEquals(1, opts.size());
        assertEquals(1L, opts.get(0).getId());
    }

    @Test
    void updateClearsNullableField() {
        when(repo.findById(1L)).thenReturn(movie());
        MovieSaveCommand cmd = new MovieSaveCommand("流浪地球3", null, null, 173,
                LocalDate.of(2026, 9, 2), 1);
        service.update(1L, cmd);
        // updateAllColumns 用 LambdaUpdateWrapper 显式 SET 全部字段（null 也会写入，区别于 updateById 跳过 null）
        verify(repo).updateAllColumns(eq(1L), same(cmd));
    }

    private Movie movie() {
        return new Movie(1L, "星际穿越", "http://x/p.jpg", "desc", 169,
                LocalDate.of(2026, 9, 1), 1, 0, null, null);
    }

    private MovieSaveCommand command() {
        return new MovieSaveCommand("流浪地球3", "http://x/p.jpg", "desc", 173,
                LocalDate.of(2026, 9, 2), 1);
    }
}

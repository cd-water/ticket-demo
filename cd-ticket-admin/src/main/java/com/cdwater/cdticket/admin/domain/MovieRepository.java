package com.cdwater.cdticket.admin.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.application.dto.MovieSaveCommand;
import com.cdwater.cdticket.admin.domain.entity.Movie;

import java.util.List;

public interface MovieRepository {
    Movie findById(Long id);
    IPage<Movie> pageByTitleAndStatus(Page<Movie> page, String title, Integer status);
    List<Movie> listOptions();
    Movie save(Movie movie);
    void deleteById(Long id);

    /** 使用 LambdaUpdateWrapper 显式 SET 全部字段，null 值也会写入数据库 */
    void updateAllColumns(Long id, MovieSaveCommand command);
}

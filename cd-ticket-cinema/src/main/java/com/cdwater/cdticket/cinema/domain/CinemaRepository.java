package com.cdwater.cdticket.cinema.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.cinema.infrastructure.entity.Cinema;

public interface CinemaRepository {
    Cinema findById(Long id);
    IPage<Cinema> pageByName(Page<Cinema> page, String name);
    Cinema save(Cinema cinema);
    void deleteById(Long id);
}

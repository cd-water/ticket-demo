package com.cdwater.cdticket.admin.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.infrastructure.entity.Cinema;

public interface CinemaRepository {
    Cinema findById(Long id);
    IPage<Cinema> pageByName(Page<Cinema> page, String name);
    Cinema save(Cinema cinema);
    void deleteById(Long id);
}

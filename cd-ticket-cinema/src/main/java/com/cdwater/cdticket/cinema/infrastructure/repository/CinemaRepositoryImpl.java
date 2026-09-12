package com.cdwater.cdticket.cinema.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.cinema.domain.CinemaRepository;
import com.cdwater.cdticket.cinema.infrastructure.entity.Cinema;
import com.cdwater.cdticket.cinema.infrastructure.mapper.CinemaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CinemaRepositoryImpl implements CinemaRepository {

    private final CinemaMapper cinemaMapper;

    @Override
    public Cinema findById(Long id) {
        return cinemaMapper.selectById(id);
    }

    @Override
    public IPage<Cinema> pageByName(Page<Cinema> page, String name) {
        return cinemaMapper.selectPage(page, new LambdaQueryWrapper<Cinema>()
                .like(name != null && !name.isBlank(), Cinema::getName, name)
                .orderByDesc(Cinema::getCreateTime));
    }

    @Override
    public Cinema save(Cinema cinema) {
        if (cinema.getId() == null) {
            cinemaMapper.insert(cinema);
        } else {
            cinemaMapper.updateById(cinema);
        }
        return cinema;
    }

    @Override
    public void deleteById(Long id) {
        cinemaMapper.deleteById(id);
    }
}

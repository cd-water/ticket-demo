package com.cdwater.cdticket.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.domain.HallRepository;
import com.cdwater.cdticket.admin.domain.entity.Hall;
import com.cdwater.cdticket.admin.infrastructure.mapper.HallMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HallRepositoryImpl implements HallRepository {

    private final HallMapper hallMapper;

    @Override
    public List<Hall> listByCinemaId(Long cinemaId) {
        return hallMapper.selectList(new LambdaQueryWrapper<Hall>()
                .eq(Hall::getCinemaId, cinemaId)
                .orderByAsc(Hall::getId));
    }

    @Override
    public Hall findById(Long id) {
        return hallMapper.selectById(id);
    }

    @Override
    public Hall save(Hall hall) {
        if (hall.getId() == null) {
            hallMapper.insert(hall);
        } else {
            hallMapper.updateById(hall);
        }
        return hall;
    }

    @Override
    public void deleteById(Long id) {
        hallMapper.deleteById(id);
    }
}

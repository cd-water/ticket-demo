package com.cdwater.cdticket.app.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.app.domain.model.Hall;
import com.cdwater.cdticket.app.domain.model.Screening;
import com.cdwater.cdticket.app.domain.model.SeatConfig;
import com.cdwater.cdticket.app.domain.repository.ScreeningRepository;
import com.cdwater.cdticket.app.infrastructure.mapper.HallMapper;
import com.cdwater.cdticket.app.infrastructure.mapper.ScreeningMapper;
import com.cdwater.cdticket.app.infrastructure.mapper.SeatConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScreeningRepositoryImpl implements ScreeningRepository {

    private final ScreeningMapper screeningMapper;
    private final HallMapper hallMapper;
    private final SeatConfigMapper seatConfigMapper;

    @Override
    public Screening findById(Long id) {
        return screeningMapper.selectById(id);
    }

    @Override
    public Hall findHall(Long hallId) {
        return hallMapper.selectById(hallId);
    }

    @Override
    public List<SeatConfig> findSeatConfigs(Long hallId) {
        return seatConfigMapper.selectList(new LambdaQueryWrapper<SeatConfig>()
                .eq(SeatConfig::getHallId, hallId)
                .orderByAsc(SeatConfig::getSeatRow, SeatConfig::getSeatCol));
    }
}

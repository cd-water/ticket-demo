package com.cdwater.cdticket.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.domain.SeatConfigRepository;
import com.cdwater.cdticket.admin.domain.entity.SeatConfig;
import com.cdwater.cdticket.admin.infrastructure.mapper.SeatConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SeatConfigRepositoryImpl implements SeatConfigRepository {

    private final SeatConfigMapper seatConfigMapper;

    @Override
    public List<SeatConfig> listByHallId(Long hallId) {
        return seatConfigMapper.selectList(new LambdaQueryWrapper<SeatConfig>()
                .eq(SeatConfig::getHallId, hallId)
                .orderByAsc(SeatConfig::getSeatRow)
                .orderByAsc(SeatConfig::getSeatCol));
    }

    @Override
    public void deleteByHallId(Long hallId) {
        seatConfigMapper.delete(new LambdaQueryWrapper<SeatConfig>()
                .eq(SeatConfig::getHallId, hallId));
    }

    /** 循环插入（单影厅 ≤676 格）；ponytail: 量大再换批量 SQL */
    @Override
    public void batchInsert(List<SeatConfig> seats) {
        for (SeatConfig seat : seats) {
            seatConfigMapper.insert(seat);
        }
    }
}

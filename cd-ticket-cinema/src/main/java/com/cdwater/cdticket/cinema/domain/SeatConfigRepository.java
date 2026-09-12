package com.cdwater.cdticket.cinema.domain;

import com.cdwater.cdticket.cinema.infrastructure.entity.SeatConfig;

import java.util.List;

public interface SeatConfigRepository {
    List<SeatConfig> listByHallId(Long hallId);
    void deleteByHallId(Long hallId);
    void batchInsert(List<SeatConfig> seats);
}

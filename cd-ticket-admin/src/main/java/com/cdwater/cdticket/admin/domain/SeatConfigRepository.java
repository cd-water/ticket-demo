package com.cdwater.cdticket.admin.domain;

import com.cdwater.cdticket.admin.infrastructure.entity.SeatConfig;

import java.util.List;

public interface SeatConfigRepository {
    List<SeatConfig> listByHallId(Long hallId);
    void deleteByHallId(Long hallId);
    void batchInsert(List<SeatConfig> seats);
}

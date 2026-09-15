package com.cdwater.cdticket.app.domain.repository;

import com.cdwater.cdticket.app.domain.model.Hall;
import com.cdwater.cdticket.app.domain.model.Screening;
import com.cdwater.cdticket.app.domain.model.SeatConfig;

import java.util.List;

public interface ScreeningRepository {

    Screening findById(Long id);

    Hall findHall(Long hallId);

    /** 影厅座位模板 */
    List<SeatConfig> findSeatConfigs(Long hallId);
}

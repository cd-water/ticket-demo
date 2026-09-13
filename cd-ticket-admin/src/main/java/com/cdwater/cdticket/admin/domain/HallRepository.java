package com.cdwater.cdticket.admin.domain;

import com.cdwater.cdticket.admin.infrastructure.entity.Hall;

import java.util.List;

public interface HallRepository {
    List<Hall> listByCinemaId(Long cinemaId);
    Hall findById(Long id);
    Hall save(Hall hall);
    void deleteById(Long id);
}

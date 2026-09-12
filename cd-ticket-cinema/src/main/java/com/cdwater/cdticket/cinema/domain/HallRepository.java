package com.cdwater.cdticket.cinema.domain;

import com.cdwater.cdticket.cinema.infrastructure.entity.Hall;

import java.util.List;

public interface HallRepository {
    List<Hall> listByCinemaId(Long cinemaId);
    Hall findById(Long id);
    Hall save(Hall hall);
    void deleteById(Long id);
}

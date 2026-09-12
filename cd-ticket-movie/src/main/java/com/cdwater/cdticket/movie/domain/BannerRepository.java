package com.cdwater.cdticket.movie.domain;

import com.cdwater.cdticket.movie.infrastructure.entity.Banner;

import java.util.List;

public interface BannerRepository {
    /** sort asc, id desc */
    List<Banner> list();
    Banner findById(Long id);
    Banner save(Banner banner);
    void deleteById(Long id);
}

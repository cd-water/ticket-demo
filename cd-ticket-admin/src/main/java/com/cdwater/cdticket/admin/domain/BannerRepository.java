package com.cdwater.cdticket.admin.domain;

import com.cdwater.cdticket.admin.domain.entity.Banner;

import java.util.List;

public interface BannerRepository {
    /** sort asc, id desc */
    List<Banner> list();
    Banner findById(Long id);
    Banner save(Banner banner);
    void deleteById(Long id);
}

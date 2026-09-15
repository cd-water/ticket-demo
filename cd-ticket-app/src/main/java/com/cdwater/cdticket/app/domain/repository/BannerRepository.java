package com.cdwater.cdticket.app.domain.repository;

import com.cdwater.cdticket.app.domain.model.Banner;

import java.util.List;

public interface BannerRepository {

    /** 启用中的轮播图（status=1，按 sort 升序） */
    List<Banner> listEnabled();
}

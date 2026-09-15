package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.BannerVO;
import com.cdwater.cdticket.app.domain.model.Banner;
import com.cdwater.cdticket.app.domain.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    /** 启用中的轮播图（status=1，按 sort 升序） */
    public List<BannerVO> list() {
        return bannerRepository.listEnabled().stream().map(b -> {
            BannerVO vo = new BannerVO();
            vo.setId(b.getId());
            vo.setImage(b.getImage());
            vo.setLinkUrl(b.getLinkUrl());
            return vo;
        }).toList();
    }
}

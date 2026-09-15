package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.BannerVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {

    /** 启用中的轮播图（status=1，按 sort 升序） */
    public List<BannerVO> list() {
        return List.of();
    }
}

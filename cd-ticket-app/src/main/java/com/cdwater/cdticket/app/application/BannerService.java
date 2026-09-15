package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.BannerVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {

    // ponytail: 内存 mock,数据重启即丢;接入 t_banner 表后改为 repository 查询
    private static final List<BannerVO> MOCK_BANNERS = List.of(
        banner(1L, "https://cdn.example.com/banner1.jpg", "https://www.baidu.com/"),
        banner(2L, "https://cdn.example.com/banner2.jpg", "https://www.baidu.com/"),
        banner(3L, "https://cdn.example.com/banner3.jpg", "https://www.baidu.com/")
    );

    private static BannerVO banner(Long id, String image, String linkUrl) {
        BannerVO vo = new BannerVO();
        vo.setId(id);
        vo.setImage(image);
        vo.setLinkUrl(linkUrl);
        return vo;
    }

    /** 启用中的轮播图（status=1，按 sort 升序） */
    public List<BannerVO> list() {
        return MOCK_BANNERS;
    }
}

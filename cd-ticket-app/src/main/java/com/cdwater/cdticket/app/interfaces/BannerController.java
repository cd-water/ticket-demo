package com.cdwater.cdticket.app.interfaces;

import com.cdwater.cdticket.app.common.Result;
import com.cdwater.cdticket.app.application.BannerService;
import com.cdwater.cdticket.app.application.dto.BannerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public Result<List<BannerVO>> list() {
        return Result.success(bannerService.list());
    }
}

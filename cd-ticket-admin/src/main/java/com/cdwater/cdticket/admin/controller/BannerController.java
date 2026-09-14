package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.BannerService;
import com.cdwater.cdticket.admin.dto.banner.BannerVO;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.banner.BannerSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public Result<List<BannerVO>> list() {
        return Result.success(bannerService.list());
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody @Valid BannerSaveRequest req) {
        bannerService.save(req);
        return Result.success();
    }
}

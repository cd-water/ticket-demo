package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.security.AdminAuthorizer;
import com.cdwater.cdticket.admin.service.BannerAdminService;
import com.cdwater.cdticket.admin.dto.banner.BannerVO;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.banner.BannerSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerAdminService bannerAdminService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<List<BannerVO>> list() {
        adminAuthorizer.requireSuperAdmin();
        return Result.success(bannerAdminService.list());
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid BannerSaveRequest req) {
        adminAuthorizer.requireSuperAdmin();
        bannerAdminService.create(req);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid BannerSaveRequest req) {
        adminAuthorizer.requireSuperAdmin();
        bannerAdminService.update(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requireSuperAdmin();
        bannerAdminService.delete(id);
        return Result.success();
    }

}

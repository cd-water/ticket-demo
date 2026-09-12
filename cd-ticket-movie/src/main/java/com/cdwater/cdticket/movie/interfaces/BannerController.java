package com.cdwater.cdticket.movie.interfaces;

import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.api.Result;
import com.cdwater.cdticket.movie.application.BannerService;
import com.cdwater.cdticket.movie.application.dto.BannerSaveCommand;
import com.cdwater.cdticket.movie.application.dto.BannerVO;
import com.cdwater.cdticket.movie.interfaces.dto.BannerSaveRequest;
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

    private final BannerService bannerService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<List<BannerVO>> list() {
        adminAuthorizer.requireSuperAdmin();
        return Result.success(bannerService.list());
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid BannerSaveRequest req) {
        adminAuthorizer.requireSuperAdmin();
        bannerService.create(toCommand(req));
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid BannerSaveRequest req) {
        adminAuthorizer.requireSuperAdmin();
        bannerService.update(id, toCommand(req));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requireSuperAdmin();
        bannerService.delete(id);
        return Result.success();
    }

    private BannerSaveCommand toCommand(BannerSaveRequest req) {
        return new BannerSaveCommand(req.getImage(), req.getLinkUrl(), req.getSort(), req.getStatus());
    }
}

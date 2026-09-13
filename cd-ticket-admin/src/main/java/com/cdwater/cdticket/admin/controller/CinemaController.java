package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.security.AdminAuthorizer;
import com.cdwater.cdticket.admin.service.CinemaAdminService;
import com.cdwater.cdticket.admin.dto.cinema.CinemaVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.cinema.CinemaSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaAdminService cinemaService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping("/simple")
    public Result<List<CinemaVO>> simple() {
        return Result.success(cinemaService.listAll());
    }

    @GetMapping
    public Result<PageResult<CinemaVO>> page(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String name) {
        adminAuthorizer.requirePlatformAdmin();
        return Result.success(cinemaService.page(page, size, name));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid CinemaSaveRequest req) {
        adminAuthorizer.requirePlatformAdmin();
        cinemaService.create(req);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CinemaSaveRequest req) {
        adminAuthorizer.requirePlatformAdmin();
        cinemaService.update(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requirePlatformAdmin();
        cinemaService.delete(id);
        return Result.success();
    }

}

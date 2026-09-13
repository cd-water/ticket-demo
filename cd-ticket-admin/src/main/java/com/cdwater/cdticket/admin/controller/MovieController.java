package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.security.AdminAuthorizer;
import com.cdwater.cdticket.admin.service.MovieAdminService;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.movie.MovieSaveRequest;
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

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieAdminService movieAdminService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<PageResult<MovieVO>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String title,
                                            @RequestParam(required = false) Integer status) {
        adminAuthorizer.requirePlatformAdmin();
        return Result.success(movieAdminService.page(page, size, title, status));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid MovieSaveRequest req) {
        adminAuthorizer.requirePlatformAdmin();
        movieAdminService.create(req);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid MovieSaveRequest req) {
        adminAuthorizer.requirePlatformAdmin();
        movieAdminService.update(id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requirePlatformAdmin();
        movieAdminService.delete(id);
        return Result.success();
    }

}

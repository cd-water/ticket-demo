package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.MovieService;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.movie.MovieSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public Result<PageResult<MovieVO>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String title,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(movieService.page(page, size, title, status));
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public Result<Void> save(@RequestBody @Valid MovieSaveRequest req) {
        movieService.save(req);
        return Result.success();
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        movieService.delete(id);
        return Result.success();
    }
}

package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.ScreeningService;
import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.dto.screening.ScreeningSaveRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/screenings")
@RequiredArgsConstructor
public class ScreeningController {
    private final ScreeningService screeningService;

    @GetMapping
    @PreAuthorize("hasAuthority('CINEMA_ADMIN')")
    public Result<PageResult<ScreeningVO>> page(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) Long movieId) {
        return Result.success(screeningService.page(page, size, movieId));
    }

    @GetMapping("/movie-options")
    @PreAuthorize("hasAuthority('CINEMA_ADMIN')")
    public Result<List<MovieOption>> movieOptions() {
        return Result.success(screeningService.movieOptions());
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('CINEMA_ADMIN')")
    public Result<Void> save(@RequestBody @Valid ScreeningSaveRequest req) {
        screeningService.save(req);
        return Result.success();
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('CINEMA_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        screeningService.delete(id);
        return Result.success();
    }
}

package com.cdwater.cdticket.admin.interfaces.controller;

import com.cdwater.cdticket.admin.application.AdminAuthorizer;
import com.cdwater.cdticket.admin.application.ScreeningAdminService;
import com.cdwater.cdticket.admin.application.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.application.dto.PageResult;
import com.cdwater.cdticket.admin.application.dto.Result;
import com.cdwater.cdticket.admin.application.dto.screening.ScreeningSaveCommand;
import com.cdwater.cdticket.admin.application.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.interfaces.dto.screening.ScreeningSaveRequest;
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
@RequestMapping("/api/admin/screenings")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningAdminService screeningAdminService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<PageResult<ScreeningVO>> page(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) Long movieId) {
        adminAuthorizer.requireCinemaAdmin();
        return Result.success(screeningAdminService.page(page, size, movieId));
    }

    @GetMapping("/movie-options")
    public Result<List<MovieOption>> movieOptions() {
        adminAuthorizer.requireCinemaAdmin();
        return Result.success(screeningAdminService.movieOptions());
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid ScreeningSaveRequest req) {
        adminAuthorizer.requireCinemaAdmin();
        screeningAdminService.create(toCommand(req));
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ScreeningSaveRequest req) {
        adminAuthorizer.requireCinemaAdmin();
        screeningAdminService.update(id, toCommand(req));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requireCinemaAdmin();
        screeningAdminService.delete(id);
        return Result.success();
    }

    private ScreeningSaveCommand toCommand(ScreeningSaveRequest req) {
        return new ScreeningSaveCommand(req.getMovieId(), req.getHallId(),
                req.getStartTime(), req.getPrice());
    }
}

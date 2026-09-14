package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.ScreeningService;
import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.screening.ScreeningVO;
import com.cdwater.cdticket.admin.dto.screening.ScreeningSaveRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class ScreeningController {
    private final ScreeningService screeningService;

    @GetMapping("/api/admin/cinemas/{cinemaId}/screenings")
    public Result<PageResult<ScreeningVO>> pageByCinema(@PathVariable @Min(1) Long cinemaId,
                                                        @RequestParam(defaultValue = "1") @Min(1) int page,
                                                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                                                        @RequestParam(required = false) @Min(1) Long movieId) {
        return Result.success(screeningService.pageByCinema(page, size, movieId, cinemaId));
    }

    @GetMapping("/api/admin/screenings/movie-options")
    public Result<List<MovieOption>> movieOptions() {
        return Result.success(screeningService.movieOptions());
    }

    @PostMapping("/api/admin/screenings/save")
    public Result<Void> save(@RequestBody @Valid ScreeningSaveRequest req) {
        screeningService.save(req);
        return Result.success();
    }

    @PostMapping("/api/admin/screenings/{id}/delete")
    public Result<Void> delete(@PathVariable Long id) {
        screeningService.delete(id);
        return Result.success();
    }
}

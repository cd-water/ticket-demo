package com.cdwater.cdticket.screening.interfaces;

import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.api.Result;
import com.cdwater.cdticket.movie.application.dto.MovieOption;
import com.cdwater.cdticket.screening.application.ScreeningService;
import com.cdwater.cdticket.screening.application.dto.ScreeningSaveCommand;
import com.cdwater.cdticket.screening.application.dto.ScreeningVO;
import com.cdwater.cdticket.screening.interfaces.dto.ScreeningSaveRequest;
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

    private final ScreeningService screeningService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<PageResult<ScreeningVO>> page(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) Long movieId) {
        adminAuthorizer.requireCinemaAdmin();
        return Result.success(screeningService.page(page, size, movieId));
    }

    @GetMapping("/movie-options")
    public Result<List<MovieOption>> movieOptions() {
        adminAuthorizer.requireCinemaAdmin();
        return Result.success(screeningService.movieOptions());
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid ScreeningSaveRequest req) {
        adminAuthorizer.requireCinemaAdmin();
        screeningService.create(toCommand(req));
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ScreeningSaveRequest req) {
        adminAuthorizer.requireCinemaAdmin();
        screeningService.update(id, toCommand(req));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requireCinemaAdmin();
        screeningService.delete(id);
        return Result.success();
    }

    private ScreeningSaveCommand toCommand(ScreeningSaveRequest req) {
        return new ScreeningSaveCommand(req.getMovieId(), req.getHallId(),
                req.getStartTime(), req.getPrice());
    }
}

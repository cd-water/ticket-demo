package com.cdwater.cdticket.app.interfaces;

import com.cdwater.cdticket.app.common.Result;
import com.cdwater.cdticket.app.application.CinemaService;
import com.cdwater.cdticket.app.application.dto.CinemaDetailVO;
import com.cdwater.cdticket.app.application.dto.CinemaVO;
import com.cdwater.cdticket.app.application.dto.ScreeningVO;
import com.cdwater.cdticket.app.common.PageResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/cinemas")
@RequiredArgsConstructor
@Validated
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    public Result<PageResult<CinemaVO>> page(@RequestParam(defaultValue = "1") @Min(1) int page,
                                             @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return Result.success(cinemaService.page(page, size));
    }

    @GetMapping("/{id}")
    public Result<CinemaDetailVO> detail(@PathVariable Long id) {
        return Result.success(cinemaService.detail(id));
    }

    @GetMapping("/{id}/screenings")
    public Result<List<ScreeningVO>> screenings(@PathVariable Long id,
                                                @RequestParam(required = false) Long movieId) {
        return Result.success(cinemaService.screenings(id, movieId));
    }
}

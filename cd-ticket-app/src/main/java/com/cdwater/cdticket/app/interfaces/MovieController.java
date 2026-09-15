package com.cdwater.cdticket.app.interfaces;

import com.cdwater.cdticket.app.common.Result;
import com.cdwater.cdticket.app.application.MovieService;
import com.cdwater.cdticket.app.application.dto.BoxOfficeVO;
import com.cdwater.cdticket.app.application.dto.MovieDetailVO;
import com.cdwater.cdticket.app.application.dto.MovieVO;
import com.cdwater.cdticket.app.common.PageResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/movies")
@RequiredArgsConstructor
@Validated
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public Result<PageResult<MovieVO>> page(@RequestParam @Pattern(regexp = "hot|coming") String status,
                                            @RequestParam(defaultValue = "1") @Min(1) int page,
                                            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return Result.success(movieService.page(status, page, size));
    }

    @GetMapping("/{id}")
    public Result<MovieDetailVO> detail(@PathVariable Long id) {
        return Result.success(movieService.detail(id));
    }

    @GetMapping("/hot")
    public Result<List<MovieVO>> hot() {
        return Result.success(movieService.hot());
    }

    @GetMapping("/coming")
    public Result<List<MovieVO>> coming() {
        return Result.success(movieService.coming());
    }

    @GetMapping("/box-office")
    public Result<List<BoxOfficeVO>> boxOffice() {
        return Result.success(movieService.boxOffice());
    }
}

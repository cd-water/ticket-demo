package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.MovieService;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.movie.MovieSaveRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
@Validated
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public Result<PageResult<MovieVO>> page(@RequestParam(defaultValue = "1") @Min(1) int page,
                                            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                                            @RequestParam(required = false) @Size(max = 100) String title,
                                            @RequestParam(required = false) @Min(0) @Max(1) Integer status) {
        return Result.success(movieService.page(page, size, title, status));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody @Valid MovieSaveRequest req) {
        movieService.save(req);
        return Result.success();
    }
}

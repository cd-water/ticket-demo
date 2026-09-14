package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.CinemaService;
import com.cdwater.cdticket.admin.dto.cinema.CinemaVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.cinema.CinemaSaveRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cinemas")
@RequiredArgsConstructor
@Validated
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    public Result<PageResult<CinemaVO>> page(@RequestParam(defaultValue = "1") @Min(1) int page,
                                             @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                                             @RequestParam(required = false) @Size(max = 100) String name,
                                             @RequestParam(required = false) @Min(0) @Max(1) Integer status) {
        return Result.success(cinemaService.page(page, size, name, status));
    }

    @GetMapping("/{id}")
    public Result<CinemaVO> get(@PathVariable Long id) {
        return Result.success(cinemaService.getCinema(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody @Valid CinemaSaveRequest req) {
        cinemaService.save(req);
        return Result.success();
    }
}

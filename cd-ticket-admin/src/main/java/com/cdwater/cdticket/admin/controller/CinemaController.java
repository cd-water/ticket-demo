package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.CinemaService;
import com.cdwater.cdticket.admin.dto.cinema.CinemaVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.cinema.CinemaSaveRequest;
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
@RequestMapping("/api/admin/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping("/simple")
    public Result<List<CinemaVO>> simple() {
        return Result.success(cinemaService.listAll());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public Result<PageResult<CinemaVO>> page(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String name) {
        return Result.success(cinemaService.page(page, size, name));
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public Result<Void> save(@RequestBody @Valid CinemaSaveRequest req) {
        cinemaService.save(req);
        return Result.success();
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        cinemaService.delete(id);
        return Result.success();
    }
}
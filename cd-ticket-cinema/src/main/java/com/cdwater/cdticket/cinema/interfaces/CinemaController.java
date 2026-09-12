package com.cdwater.cdticket.cinema.interfaces;

import com.cdwater.cdticket.cinema.application.CinemaService;
import com.cdwater.cdticket.cinema.application.dto.CinemaSaveCommand;
import com.cdwater.cdticket.cinema.application.dto.CinemaVO;
import com.cdwater.cdticket.cinema.interfaces.dto.CinemaSaveRequest;
import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.api.Result;
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

@RestController
@RequestMapping("/api/admin/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<PageResult<CinemaVO>> page(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String name) {
        adminAuthorizer.requireSuperAdmin();
        return Result.success(cinemaService.page(page, size, name));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid CinemaSaveRequest req) {
        adminAuthorizer.requireSuperAdmin();
        cinemaService.create(toCommand(req));
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid CinemaSaveRequest req) {
        adminAuthorizer.requireSuperAdmin();
        cinemaService.update(id, toCommand(req));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requireSuperAdmin();
        cinemaService.delete(id);
        return Result.success();
    }

    private CinemaSaveCommand toCommand(CinemaSaveRequest req) {
        return new CinemaSaveCommand(req.getName(), req.getAddress(), req.getStatus());
    }
}

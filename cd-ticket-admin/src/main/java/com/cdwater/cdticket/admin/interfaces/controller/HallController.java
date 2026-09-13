package com.cdwater.cdticket.admin.interfaces.controller;

import com.cdwater.cdticket.admin.application.AdminAuthorizer;
import com.cdwater.cdticket.admin.application.HallAdminService;
import com.cdwater.cdticket.admin.application.SeatConfigAdminService;
import com.cdwater.cdticket.admin.application.dto.hall.HallSaveCommand;
import com.cdwater.cdticket.admin.application.dto.hall.HallVO;
import com.cdwater.cdticket.admin.application.dto.Result;
import com.cdwater.cdticket.admin.application.dto.seat.SeatGridVO;
import com.cdwater.cdticket.admin.interfaces.dto.hall.HallSaveRequest;
import com.cdwater.cdticket.admin.interfaces.dto.seat.SeatReplaceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallAdminService hallService;
    private final SeatConfigAdminService seatConfigService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<List<HallVO>> list() {
        adminAuthorizer.requireCinemaAdmin();
        return Result.success(hallService.list());
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid HallSaveRequest req) {
        adminAuthorizer.requireCinemaAdmin();
        hallService.create(toCommand(req));
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid HallSaveRequest req) {
        adminAuthorizer.requireCinemaAdmin();
        hallService.update(id, toCommand(req));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requireCinemaAdmin();
        hallService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/seats")
    public Result<SeatGridVO> seats(@PathVariable Long id) {
        adminAuthorizer.requireCinemaAdmin();
        return Result.success(seatConfigService.getGrid(id));
    }

    @PutMapping("/{id}/seats")
    public Result<Void> replaceSeats(@PathVariable Long id, @RequestBody @Valid SeatReplaceRequest req) {
        adminAuthorizer.requireCinemaAdmin();
        seatConfigService.replace(id, req.getSeats());
        return Result.success();
    }

    private HallSaveCommand toCommand(HallSaveRequest req) {
        return new HallSaveCommand(req.getName(), req.getSeatRows(), req.getSeatCols());
    }
}

package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.HallService;
import com.cdwater.cdticket.admin.service.SeatConfigService;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.hall.HallSaveRequest;
import com.cdwater.cdticket.admin.dto.hall.SeatCellRequest;
import com.cdwater.cdticket.admin.dto.hall.SeatGridVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class HallController {

    private final HallService hallService;
    private final SeatConfigService seatConfigService;

    @GetMapping("/cinemas/{cinemaId}/halls")
    public Result<List<HallVO>> listByCinema(@PathVariable Long cinemaId) {
        return Result.success(hallService.listByCinema(cinemaId));
    }

    @PostMapping("/halls/save")
    public Result<Void> save(@RequestBody @Valid HallSaveRequest req) {
        hallService.save(req);
        return Result.success();
    }

    @GetMapping("/halls/{id}/seats")
    public Result<SeatGridVO> seats(@PathVariable Long id) {
        return Result.success(seatConfigService.getGrid(id));
    }

    @PostMapping("/halls/{id}/seats")
    public Result<Void> replaceSeats(@PathVariable Long id, @RequestBody @NotNull List<SeatCellRequest> seats) {
        seatConfigService.replace(id, seats);
        return Result.success();
    }
}

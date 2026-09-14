package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.HallService;
import com.cdwater.cdticket.admin.service.SeatConfigService;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.seat.SeatGridVO;
import com.cdwater.cdticket.admin.dto.hall.HallSaveRequest;
import com.cdwater.cdticket.admin.dto.seat.SeatReplaceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HallController {
    private final HallService hallService;
    private final SeatConfigService seatConfigService;

    @GetMapping("/api/admin/cinemas/{cinemaId}/halls")
    public Result<List<HallVO>> listByCinema(@PathVariable Long cinemaId) {
        return Result.success(hallService.listByCinema(cinemaId));
    }

    @PostMapping("/api/admin/halls/save")
    public Result<Void> save(@RequestBody @Valid HallSaveRequest req) {
        hallService.save(req);
        return Result.success();
    }

    @PostMapping("/api/admin/halls/{id}/delete")
    public Result<Void> delete(@PathVariable Long id) {
        hallService.delete(id);
        return Result.success();
    }

    @GetMapping("/api/admin/halls/{id}/seats")
    public Result<SeatGridVO> seats(@PathVariable Long id) {
        return Result.success(seatConfigService.getGrid(id));
    }

    @PostMapping("/api/admin/halls/{id}/seats")
    public Result<Void> replaceSeats(@PathVariable Long id, @RequestBody @Valid SeatReplaceRequest req) {
        seatConfigService.replace(id, req.getSeats());
        return Result.success();
    }
}

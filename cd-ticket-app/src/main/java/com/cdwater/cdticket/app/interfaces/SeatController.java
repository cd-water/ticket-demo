package com.cdwater.cdticket.app.interfaces;

import com.cdwater.cdticket.app.application.OrderService;
import com.cdwater.cdticket.app.application.dto.SeatMapVO;
import com.cdwater.cdticket.app.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/screenings")
@RequiredArgsConstructor
public class SeatController {

    private final OrderService orderService;

    @GetMapping("/{id}/seats")
    public Result<SeatMapVO> seats(@PathVariable Long id) {
        return Result.success(orderService.seatMap(id));
    }
}

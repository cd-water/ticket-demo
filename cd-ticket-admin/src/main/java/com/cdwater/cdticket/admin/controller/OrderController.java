package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.OrderService;
import com.cdwater.cdticket.admin.dto.order.OrderVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/cinemas/{cinemaId}/orders")
    public Result<PageResult<OrderVO>> pageByCinema(@PathVariable @Min(1) Long cinemaId,
                                                    @RequestParam(defaultValue = "1") @Min(1) int page,
                                                    @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                                                    @RequestParam(required = false) Long orderNo,
                                                    @RequestParam(required = false) @Min(0) @Max(2) Integer status) {
        return Result.success(orderService.pageByCinema(page, size, orderNo, status, cinemaId));
    }
}

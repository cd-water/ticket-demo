package com.cdwater.cdticket.app.interfaces;

import com.cdwater.cdticket.app.application.OrderService;
import com.cdwater.cdticket.app.application.PayService;
import com.cdwater.cdticket.app.application.dto.OrderVO;
import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.common.Result;
import com.cdwater.cdticket.app.common.util.SecurityUtils;
import com.cdwater.cdticket.app.interfaces.dto.CreateOrderRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;
    private final PayService payService;

    @PostMapping
    public Result<OrderVO> create(@RequestBody @Valid CreateOrderRequest req) {
        return Result.success(orderService.create(SecurityUtils.getCurrentId(), req));
    }

    @GetMapping
    public Result<PageResult<OrderVO>> page(@RequestParam(defaultValue = "1") @Min(1) int page,
                                            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
                                            @RequestParam(required = false) @Min(0) @Max(2) Integer status) {
        return Result.success(orderService.page(SecurityUtils.getCurrentId(), status, page, size));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detail(SecurityUtils.getCurrentId(), id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(SecurityUtils.getCurrentId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/pay")
    public Result<OrderVO> pay(@PathVariable Long id) {
        return Result.success(payService.pay(SecurityUtils.getCurrentId(), id));
    }
}

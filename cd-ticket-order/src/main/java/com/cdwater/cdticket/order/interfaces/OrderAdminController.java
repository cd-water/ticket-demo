package com.cdwater.cdticket.order.interfaces;

import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.api.Result;
import com.cdwater.cdticket.order.application.OrderQueryService;
import com.cdwater.cdticket.order.application.dto.OrderAdminRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderQueryService orderQueryService;

    @GetMapping
    public Result<PageResult<OrderAdminRecord>> page(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String orderNo,
                                                     @RequestParam(required = false) Integer status) {
        return Result.success(orderQueryService.page(page, size, orderNo, status));
    }
}

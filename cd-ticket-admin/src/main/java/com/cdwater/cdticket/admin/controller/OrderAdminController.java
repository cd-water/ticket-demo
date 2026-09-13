package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.OrderQueryAdminService;
import com.cdwater.cdticket.admin.dto.order.OrderAdminRecord;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderQueryAdminService orderQueryAdminService;

    @GetMapping
    public Result<PageResult<OrderAdminRecord>> page(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String orderNo,
                                                     @RequestParam(required = false) Integer status) {
        return Result.success(orderQueryAdminService.page(page, size, orderNo, status));
    }
}

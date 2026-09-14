package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.service.OrderQueryService;
import com.cdwater.cdticket.admin.dto.order.OrderVO;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('CINEMA_ADMIN')")
    public Result<PageResult<OrderVO>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String orderNo,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(orderQueryService.page(page, size, orderNo, status));
    }
}

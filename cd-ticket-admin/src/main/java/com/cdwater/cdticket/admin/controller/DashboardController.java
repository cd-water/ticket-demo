package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.dashboard.DashboardVO;
import com.cdwater.cdticket.admin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public Result<DashboardVO> overview() {
        return Result.success(dashboardService.overview());
    }
}

package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.order.OrderAdminRecord;
import com.cdwater.cdticket.admin.mapper.OrderAdminMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderAdminMapper orderAdminMapper;

    public PageResult<OrderAdminRecord> page(int page, int size, String orderNo, Integer status) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        Long cinemaId = SecurityUtils.isPlatformAdmin() ? null : SecurityUtils.getCinemaId();
        var p = orderAdminMapper.selectPage(Page.of(page, size), orderNo, status, cinemaId);
        return PageResult.of(p);
    }
}

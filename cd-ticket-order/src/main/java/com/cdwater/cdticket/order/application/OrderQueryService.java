package com.cdwater.cdticket.order.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.api.ResultCode;
import com.cdwater.cdticket.common.exception.BizException;
import com.cdwater.cdticket.order.application.dto.OrderAdminRecord;
import com.cdwater.cdticket.order.infrastructure.mapper.OrderAdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderAdminMapper orderAdminMapper;
    private final AdminAuthorizer adminAuthorizer;

    /** 影院管理员强制本影院；超管查全部 */
    public PageResult<OrderAdminRecord> page(int page, int size, String orderNo, Integer status) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        var current = adminAuthorizer.currentAdmin();
        Long cinemaId = current.getRole() == 1 ? current.getCinemaId() : null;
        var p = orderAdminMapper.selectPage(Page.of(page, size), orderNo, status, cinemaId);
        return PageResult.of(p);
    }
}

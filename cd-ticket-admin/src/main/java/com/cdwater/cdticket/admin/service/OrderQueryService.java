package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.dto.order.OrderVO;
import com.cdwater.cdticket.admin.mapper.OrderAdminMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderAdminMapper orderAdminMapper;

    public PageResult<OrderVO> page(int page, int size, String orderNo, Integer status) {
        PageResult.check(page, size);
        IPage<OrderVO> p = orderAdminMapper.selectPage(Page.of(page, size), orderNo, status,
                SecurityUtils.getCinemaId());
        return PageResult.of(p);
    }
}

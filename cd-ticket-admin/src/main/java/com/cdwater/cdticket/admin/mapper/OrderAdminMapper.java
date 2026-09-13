package com.cdwater.cdticket.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.dto.order.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderAdminMapper {
    IPage<OrderVO> selectPage(Page<OrderVO> page,
                                       @Param("orderNo") String orderNo,
                                       @Param("status") Integer status,
                                       @Param("cinemaId") Long cinemaId);
}

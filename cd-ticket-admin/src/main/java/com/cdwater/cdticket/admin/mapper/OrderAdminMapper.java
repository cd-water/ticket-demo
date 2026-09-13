package com.cdwater.cdticket.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.dto.order.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderAdminMapper {

    /** 分页查询订单（JOIN t_user 取手机号/昵称）。分页由 PaginationInnerInterceptor 处理（Page 为首参）。 */
    IPage<OrderVO> selectPage(Page<OrderVO> page,
                                       @Param("orderNo") String orderNo,
                                       @Param("status") Integer status,
                                       @Param("cinemaId") Long cinemaId);
}

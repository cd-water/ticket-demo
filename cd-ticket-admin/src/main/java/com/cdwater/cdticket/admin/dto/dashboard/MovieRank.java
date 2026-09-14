package com.cdwater.cdticket.admin.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

/** 影片票房排行条目（仅已支付订单） */
@Data
public class MovieRank {
    private String title;
    /** 已支付订单数 */
    private long orderCount;
    /** 已支付订单营收 */
    private BigDecimal revenue;
}

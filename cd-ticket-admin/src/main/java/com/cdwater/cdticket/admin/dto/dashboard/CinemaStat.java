package com.cdwater.cdticket.admin.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

/** 单个影院的经营汇总（仅已支付订单计营收与订单数） */
@Data
public class CinemaStat {
    private String name;
    /** 已支付订单数 */
    private long orderCount;
    /** 已支付订单营收 */
    private BigDecimal revenue;
}

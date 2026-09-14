package com.cdwater.cdticket.admin.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 单日订单统计（仅已支付订单） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyOrderStat {
    /** yyyy-MM-dd */
    private String date;
    private long orderCount;
    private BigDecimal revenue;
}

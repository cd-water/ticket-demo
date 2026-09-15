package com.cdwater.cdticket.app.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 支付流水号（雪花ID） */
    private Long paymentNo;
    private Long orderId;
    /** 1-支付宝 2-微信（模拟支付固定 1） */
    private Integer channel;
    private BigDecimal amount;
    /** 0-待支付 1-成功 2-失败 3-已关闭 */
    private Integer status;
    private String outTradeNo;
    private String tradeNo;
    private LocalDateTime paidTime;
    private String notifyRaw;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

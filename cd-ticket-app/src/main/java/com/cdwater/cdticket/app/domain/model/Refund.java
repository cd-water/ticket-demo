package com.cdwater.cdticket.app.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_refund")
public class Refund {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 退款流水号（雪花ID） */
    private Long refundNo;
    private Long paymentId;
    private Long orderId;
    /** 原支付渠道（1-支付宝 2-微信） */
    private Integer channel;
    private BigDecimal refundAmount;
    /** 0-待退款 1-退款中 2-成功 3-失败 */
    private Integer status;
    private String outRefundNo;
    private String tradeNo;
    private String reason;
    private LocalDateTime refundTime;
    private String notifyRaw;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

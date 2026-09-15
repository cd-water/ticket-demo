package com.cdwater.cdticket.app.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单号（雪花ID） */
    private Long orderNo;
    private Long userId;
    private Long screeningId;
    private Long movieId;
    private Long cinemaId;
    /** 0-待支付 1-已支付 2-已取消 */
    private Integer status;
    private BigDecimal totalAmount;
    /** 乐观锁版本号（预留；支付/关单以 status CAS 保证并发安全） */
    private Integer version;
    private LocalDateTime payExpireTime;
    private LocalDateTime payTime;
    /** 取票码（支付成功时生成，8位数字） */
    private String ticketCode;
    /** 取消原因（超时关单/用户取消） */
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

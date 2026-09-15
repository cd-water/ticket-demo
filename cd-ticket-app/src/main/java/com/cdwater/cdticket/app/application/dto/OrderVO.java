package com.cdwater.cdticket.app.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 订单响应（列表/详情/创建/支付共用） */
@Data
public class OrderVO {
    private Long id;
    /** 订单号（雪花ID，字符串传输） */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long orderNo;
    private Integer status;
    private BigDecimal totalAmount;
    private Long movieId;
    private String movieTitle;
    private String moviePoster;
    private Long cinemaId;
    private String cinemaName;
    private String hallName;
    private Long screeningId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /** 座位明细（详情/创建/支付响应） */
    private List<OrderItemVO> items;
    /** 座位号列表（列表响应） */
    private List<String> seats;
    private String ticketCode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payExpireTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    private String cancelReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

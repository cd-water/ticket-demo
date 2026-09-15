package com.cdwater.cdticket.app.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单列表 JOIN 投影（t_order + 电影/影院/影厅/排场） */
@Data
public class OrderRowVO {
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long orderNo;
    private Long userId;
    private Long screeningId;
    private Long movieId;
    private String movieTitle;
    private String moviePoster;
    private Long cinemaId;
    private String cinemaName;
    private String hallName;
    private LocalDateTime startTime;
    private Integer status;
    private BigDecimal totalAmount;
    private LocalDateTime payExpireTime;
    private LocalDateTime payTime;
    private String ticketCode;
    private String cancelReason;
    private LocalDateTime createTime;
}

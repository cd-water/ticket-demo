package com.cdwater.cdticket.admin.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderAdminRecord {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long screeningId;
    private Long movieId;
    private String movieTitle;
    private Long cinemaId;
    private Integer status;
    private BigDecimal totalAmount;
    private LocalDateTime payExpireTime;
    private LocalDateTime payTime;
    private Integer cancelType;
    private LocalDateTime createTime;
    private String userPhone;
    private String userNickname;
}

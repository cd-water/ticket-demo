package com.cdwater.cdticket.admin.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemVO {
    private Long id;
    private Long orderId;
    private Long screeningId;
    private Integer seatRow;
    private Integer seatCol;
    /** 展示座位号，如 "3排5座" */
    private String seatNo;
    private BigDecimal price;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createTime;
}

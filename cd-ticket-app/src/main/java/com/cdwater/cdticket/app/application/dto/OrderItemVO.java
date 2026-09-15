package com.cdwater.cdticket.app.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Integer seatRow;
    private Integer seatCol;
    private String seatNo;
    private BigDecimal price;
}

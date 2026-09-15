package com.cdwater.cdticket.app.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BoxOfficeVO {

    private Long id;
    private String title;
    private BigDecimal boxOffice;
}

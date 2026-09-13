package com.cdwater.cdticket.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HallVO {

    private Long id;
    private Long cinemaId;
    private String name;
    private Integer seatRows;
    private Integer seatCols;
    private Integer status;
}
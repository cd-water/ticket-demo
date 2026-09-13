package com.cdwater.cdticket.admin.dto.hall;

import lombok.Data;

@Data
public class HallVO {
    private Long id;
    private Long cinemaId;
    private String name;
    private Integer seatRows;
    private Integer seatCols;
    private Integer status;
}

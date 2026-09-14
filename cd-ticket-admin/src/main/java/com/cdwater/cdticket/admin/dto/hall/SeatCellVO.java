package com.cdwater.cdticket.admin.dto.hall;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatCellVO {
    private int row;
    private int col;
    private String seatNo;
    private int status;
}

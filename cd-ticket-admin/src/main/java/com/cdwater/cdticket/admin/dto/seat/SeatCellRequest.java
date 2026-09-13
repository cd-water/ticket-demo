package com.cdwater.cdticket.admin.dto.seat;

import lombok.Data;

@Data
public class SeatCellRequest {
    private int row;
    private int col;
    private int status;
}

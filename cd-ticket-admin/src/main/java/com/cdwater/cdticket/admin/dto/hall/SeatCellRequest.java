package com.cdwater.cdticket.admin.dto.hall;

import lombok.Data;

@Data
public class SeatCellRequest {
    private int row;
    private int col;
    private int status;
}

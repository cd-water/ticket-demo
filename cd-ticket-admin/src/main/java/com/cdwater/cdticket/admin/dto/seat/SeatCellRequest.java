package com.cdwater.cdticket.admin.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatCellRequest {

    private int row;
    private int col;
    private int status;
}
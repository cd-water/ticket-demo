package com.cdwater.cdticket.cinema.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatCellVO {

    private int row;
    private int col;
    private String seatNo;
    private int status;
}
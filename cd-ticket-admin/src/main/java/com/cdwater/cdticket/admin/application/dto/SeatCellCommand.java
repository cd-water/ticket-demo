package com.cdwater.cdticket.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatCellCommand {

    private int row;
    private int col;
    private int status;
}
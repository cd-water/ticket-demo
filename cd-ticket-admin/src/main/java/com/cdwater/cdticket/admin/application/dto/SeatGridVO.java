package com.cdwater.cdticket.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatGridVO {

    private int rows;
    private int cols;
    private List<SeatCellVO> seats;
}
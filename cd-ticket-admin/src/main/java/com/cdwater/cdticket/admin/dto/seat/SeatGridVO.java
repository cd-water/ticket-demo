package com.cdwater.cdticket.admin.dto.seat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SeatGridVO {
    private int rows;
    private int cols;
    private List<SeatCellVO> seats;
}

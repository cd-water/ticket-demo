package com.cdwater.cdticket.app.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatPos {
    private Integer seatRow;
    private Integer seatCol;
}

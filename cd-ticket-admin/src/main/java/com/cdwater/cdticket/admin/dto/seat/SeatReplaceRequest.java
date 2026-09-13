package com.cdwater.cdticket.admin.dto.seat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SeatReplaceRequest {
    @NotNull
    private List<SeatCellRequest> seats;
}

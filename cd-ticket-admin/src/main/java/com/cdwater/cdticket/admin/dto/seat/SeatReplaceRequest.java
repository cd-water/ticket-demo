package com.cdwater.cdticket.admin.dto.seat;

import com.cdwater.cdticket.admin.dto.seat.SeatCellRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SeatReplaceRequest {

    
    private List<SeatCellRequest> seats;
}

package com.cdwater.cdticket.admin.dto.seat;

import com.cdwater.cdticket.admin.dto.seat.SeatCellItem;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SeatReplaceRequest {

    @NotNull(message = "座位列表不能为空")
    private List<SeatCellItem> seats;
}

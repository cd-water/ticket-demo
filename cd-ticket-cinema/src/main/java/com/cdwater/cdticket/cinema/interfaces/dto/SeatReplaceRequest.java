package com.cdwater.cdticket.cinema.interfaces.dto;

import com.cdwater.cdticket.cinema.application.dto.SeatCellCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SeatReplaceRequest {

    @NotNull(message = "座位列表不能为空")
    private List<SeatCellCommand> seats;
}

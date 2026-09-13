package com.cdwater.cdticket.admin.interfaces.dto;

import com.cdwater.cdticket.admin.application.dto.SeatCellCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SeatReplaceRequest {

    @NotNull(message = "座位列表不能为空")
    private List<SeatCellCommand> seats;
}

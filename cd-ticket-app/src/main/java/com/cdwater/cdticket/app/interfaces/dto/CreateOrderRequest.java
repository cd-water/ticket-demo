package com.cdwater.cdticket.app.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "排场ID不能为空")
    private Long screeningId;

    @NotEmpty(message = "请选择座位")
    @Size(min = 1, max = 6, message = "单笔订单可选 1-6 个座位")
    @Valid
    private List<Seat> seats;

    @Data
    public static class Seat {
        @NotNull(message = "座位排不能为空")
        @Min(value = 1, message = "座位排非法")
        private Integer seatRow;

        @NotNull(message = "座位号不能为空")
        @Min(value = 1, message = "座位号非法")
        @Max(value = 100, message = "座位号非法")
        private Integer seatCol;
    }
}

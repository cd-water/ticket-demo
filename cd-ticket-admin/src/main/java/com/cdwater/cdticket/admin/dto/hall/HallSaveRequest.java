package com.cdwater.cdticket.admin.dto.hall;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HallSaveRequest {

    @NotBlank(message = "影厅名称不能为空")
    @Size(max = 50, message = "影厅名称不能超过50字")
    private String name;

    @NotNull(message = "排数不能为空")
    @Min(value = 1, message = "排数非法")
    @Max(value = 26, message = "排数不能超过26排")
    private Integer seatRows;

    @NotNull(message = "每排座位数不能为空")
    @Min(value = 1, message = "每排座位数非法")
    @Max(value = 26, message = "每排座位数不能超过26座")
    private Integer seatCols;
}

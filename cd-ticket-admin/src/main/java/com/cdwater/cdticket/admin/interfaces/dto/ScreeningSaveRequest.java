package com.cdwater.cdticket.admin.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScreeningSaveRequest {
    @NotNull(message = "电影不能为空")
    private Long movieId;

    @NotNull(message = "影厅不能为空")
    private Long hallId;

    @NotNull(message = "开场时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
}

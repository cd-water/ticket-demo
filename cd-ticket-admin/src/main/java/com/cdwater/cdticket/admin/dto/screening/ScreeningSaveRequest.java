package com.cdwater.cdticket.admin.dto.screening;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScreeningSaveRequest {
    private Long id;

    @NotNull
    private Long movieId;

    @NotNull
    private Long hallId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
}

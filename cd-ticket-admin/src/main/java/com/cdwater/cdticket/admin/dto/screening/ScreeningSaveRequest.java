package com.cdwater.cdticket.admin.dto.screening;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScreeningSaveRequest {
    private Long id;

    @NotNull
    private Long cinemaId;

    @NotNull
    private Long movieId;

    @NotNull
    private Long hallId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
}

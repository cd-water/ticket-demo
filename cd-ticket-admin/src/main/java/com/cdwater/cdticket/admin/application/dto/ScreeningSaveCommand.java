package com.cdwater.cdticket.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningSaveCommand {

    private Long movieId;
    private Long hallId;
    private LocalDateTime startTime;
    private BigDecimal price;
}

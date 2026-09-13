package com.cdwater.cdticket.admin.dto.screening;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScreeningSaveRequest {

    /** 修改时传入；新增不填 */
    private Long id;

    
    private Long movieId;

    
    private Long hallId;

    
    private LocalDateTime startTime;

    
    @DecimalMin(value = "0.01")
    private BigDecimal price;
}

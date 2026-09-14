package com.cdwater.cdticket.admin.dto.hall;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HallSaveRequest {
    private Long id;

    @NotNull
    private Long cinemaId;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    @Min(value = 1)
    @Max(value = 26)
    private Integer seatRows;

    @NotNull
    @Min(value = 1)
    @Max(value = 26)
    private Integer seatCols;

    @Min(value = 0)
    @Max(value = 1)
    private Integer status;
}

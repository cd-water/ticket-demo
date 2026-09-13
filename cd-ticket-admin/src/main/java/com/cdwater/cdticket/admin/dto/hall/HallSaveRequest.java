package com.cdwater.cdticket.admin.dto.hall;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HallSaveRequest {

    /** 修改时传入；新增不填 */
    private Long id;

    
    @Size(max = 50)
    private String name;

    
    @Min(value = 1)
    @Max(value = 26)
    private Integer seatRows;

    
    @Min(value = 1)
    @Max(value = 26)
    private Integer seatCols;
}

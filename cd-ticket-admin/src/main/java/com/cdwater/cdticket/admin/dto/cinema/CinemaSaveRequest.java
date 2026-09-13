package com.cdwater.cdticket.admin.dto.cinema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CinemaSaveRequest {

    /** 修改时传入；新增不填 */
    private Long id;

    
    @Size(max = 100)
    private String name;

    
    @Size(max = 255)
    private String address;

    
    @Min(value = 0)
    @Max(value = 1)
    private Integer status;
}

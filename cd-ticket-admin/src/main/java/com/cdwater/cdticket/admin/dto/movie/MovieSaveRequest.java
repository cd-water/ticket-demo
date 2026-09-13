package com.cdwater.cdticket.admin.dto.movie;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieSaveRequest {

    /** 修改时传入；新增不填 */
    private Long id;

    
    @Size(max = 100)
    private String title;

    
    @Size(max = 255)
    private String poster;

    
    @Size(max = 1024)
    private String description;

    
    @Min(value = 1)
    private Integer duration;

    
    private LocalDate releaseDate;

    
    @Min(value = 0)
    @Max(value = 1)
    private Integer status;
}

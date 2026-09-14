package com.cdwater.cdticket.admin.dto.movie;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieSaveRequest {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String poster;

    @NotBlank
    @Size(max = 1024)
    private String description;

    @NotNull
    @Min(value = 1)
    private Integer duration;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @NotNull
    @Min(value = 0)
    @Max(value = 1)
    private Integer status;
}

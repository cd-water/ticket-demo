package com.cdwater.cdticket.admin.dto.movie;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MovieVO {
    private Long id;
    private String title;
    private String poster;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    private Integer status;
    private LocalDateTime createTime;
}

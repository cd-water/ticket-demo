package com.cdwater.cdticket.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieSaveCommand {

    private String title;
    private String poster;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    private Integer status;
}
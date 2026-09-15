package com.cdwater.cdticket.app.application.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieDetailVO {

    private Long id;
    private String title;
    private String poster;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    /** hot 热映 / coming 待映 */
    private String showStatus;
}

package com.cdwater.cdticket.app.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class CinemaDetailVO {

    private Long id;
    private String name;
    private String address;
    private Integer status;
    /** 正在排片的电影（去重） */
    private List<MovieVO> movies;
}

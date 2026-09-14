package com.cdwater.cdticket.movie.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_movie")
public class Movie {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String poster;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

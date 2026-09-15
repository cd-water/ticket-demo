package com.cdwater.cdticket.app.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_movie")
public class Movie {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String poster;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    /** 0-下架 1-上架；热映/待映由 releaseDate 与当前日期比对得出 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

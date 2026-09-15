package com.cdwater.cdticket.app.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_screening")
public class Screening {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long movieId;
    private Long hallId;
    private Long cinemaId;
    private LocalDateTime startTime;
    private BigDecimal price;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

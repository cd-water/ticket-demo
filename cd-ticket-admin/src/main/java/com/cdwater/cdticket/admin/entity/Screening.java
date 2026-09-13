package com.cdwater.cdticket.admin.entity;

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
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

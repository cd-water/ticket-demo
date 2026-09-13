package com.cdwater.cdticket.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_hall")
public class Hall {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cinemaId;
    private String name;
    private Integer seatRows;
    private Integer seatCols;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

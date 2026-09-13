package com.cdwater.cdticket.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_seat_config")
public class SeatConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long hallId;
    private Integer seatRow;
    private Integer seatCol;
    private String seatNo;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

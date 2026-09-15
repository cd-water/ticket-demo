package com.cdwater.cdticket.app.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_cinema")
public class Cinema {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    /** 0-停业 1-营业 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

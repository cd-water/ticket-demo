package com.cdwater.cdticket.app.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_banner")
public class Banner {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String image;
    private String linkUrl;
    private Integer sort;
    /** 0-禁用 1-启用 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

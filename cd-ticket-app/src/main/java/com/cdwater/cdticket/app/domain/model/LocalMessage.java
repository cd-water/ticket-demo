package com.cdwater.cdticket.app.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_local_message")
public class LocalMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String msgType;
    private Long bizId;
    private String payload;
    /** 0-待发送 1-已发送 2-终态失败 */
    private Integer status;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package com.cdwater.cdticket.admin.dto.banner;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BannerVO {
    private Long id;
    private String image;
    private String linkUrl;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

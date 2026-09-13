package com.cdwater.cdticket.admin.application.dto.banner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerVO {

    private Long id;
    private String image;
    private String linkUrl;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
}
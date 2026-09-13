package com.cdwater.cdticket.admin.application.dto.banner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerSaveCommand {

    private String image;
    private String linkUrl;
    private Integer sort;
    private Integer status;
}
package com.cdwater.cdticket.admin.dto.banner;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BannerSaveRequest {

    @NotBlank(message = "图片地址不能为空")
    @Size(max = 255, message = "图片地址过长")
    private String image;

    @NotBlank(message = "跳转链接不能为空")
    @Size(max = 255, message = "跳转链接过长")
    private String linkUrl;

    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序非法")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态非法")
    @Max(value = 1, message = "状态非法")
    private Integer status;
}

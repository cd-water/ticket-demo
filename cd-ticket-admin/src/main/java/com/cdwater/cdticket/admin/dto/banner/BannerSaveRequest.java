package com.cdwater.cdticket.admin.dto.banner;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BannerSaveRequest {
    private Long id;

    @NotBlank
    @Size(max = 255)
    private String image;

    @NotBlank
    @Size(max = 255)
    private String linkUrl;

    @NotNull
    @Min(value = 0)
    private Integer sort;

    @NotNull
    @Min(value = 0)
    @Max(value = 1)
    private Integer status;
}

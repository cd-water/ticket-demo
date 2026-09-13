package com.cdwater.cdticket.admin.interfaces.dto.movie;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieSaveRequest {

    @NotBlank(message = "片名不能为空")
    @Size(max = 100, message = "片名不能超过100字")
    private String title;

    @NotBlank(message = "海报不能为空")
    @Size(max = 255, message = "海报地址过长")
    private String poster;

    @NotBlank(message = "简介不能为空")
    @Size(max = 1024, message = "简介不能超过1024字")
    private String description;

    @NotNull(message = "时长不能为空")
    @Min(value = 1, message = "时长必须大于0")
    private Integer duration;

    @NotNull(message = "上映日期不能为空")
    private LocalDate releaseDate;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态非法")
    @Max(value = 1, message = "状态非法")
    private Integer status;
}

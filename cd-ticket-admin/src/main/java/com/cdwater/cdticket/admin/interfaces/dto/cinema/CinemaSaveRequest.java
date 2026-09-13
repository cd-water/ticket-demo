package com.cdwater.cdticket.admin.interfaces.dto.cinema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CinemaSaveRequest {

    @NotBlank(message = "影院名称不能为空")
    @Size(max = 100, message = "影院名称不能超过100字")
    private String name;

    @NotBlank(message = "影院地址不能为空")
    @Size(max = 255, message = "影院地址不能超过255字")
    private String address;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态非法")
    @Max(value = 1, message = "状态非法")
    private Integer status;
}

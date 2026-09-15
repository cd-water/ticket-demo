package com.cdwater.cdticket.app.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SmsCodeRequest {

    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    private String phone;
}
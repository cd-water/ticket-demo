package com.cdwater.cdticket.admin.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminSaveRequest {
    @NotBlank
    @Size(min = 5, max = 32)
    private String username;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$")
    private String password;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer role;

    private Long cinemaId;

    public boolean isCinemaAdmin() {
        return role != null && role == 1;
    }
}

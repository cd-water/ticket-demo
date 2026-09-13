package com.cdwater.cdticket.admin.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateCommand {

    private String username;
    private String password;
    private Integer role;
    private Long cinemaId;
}
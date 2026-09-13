package com.cdwater.cdticket.admin.application.dto.cinema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaSaveCommand {

    private String name;
    private String address;
    private Integer status;
}
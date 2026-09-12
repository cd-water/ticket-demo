package com.cdwater.cdticket.cinema.application.dto;

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
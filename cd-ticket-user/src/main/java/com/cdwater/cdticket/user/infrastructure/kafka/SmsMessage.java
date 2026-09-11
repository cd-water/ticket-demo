package com.cdwater.cdticket.user.infrastructure.kafka;

import lombok.Data;

@Data
public class SmsMessage {
    private String phone;
    private String code;
}
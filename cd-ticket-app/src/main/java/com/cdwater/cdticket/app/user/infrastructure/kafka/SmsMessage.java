package com.cdwater.cdticket.app.user.infrastructure.kafka;

import lombok.Data;

@Data
public class SmsMessage {
    private String phone;
    private String code;
}
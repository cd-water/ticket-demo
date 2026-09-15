package com.cdwater.cdticket.app.infrastructure.mq;

import lombok.Data;

@Data
public class SmsMessage {
    private String phone;
    private String code;
}
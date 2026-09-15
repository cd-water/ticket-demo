package com.cdwater.cdticket.app.application;

public interface SmsSender {
    void send(String phone, String code);
}

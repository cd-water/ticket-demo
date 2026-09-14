package com.cdwater.cdticket.app.user.application;

public interface SmsSender {
    void send(String phone, String code);
}

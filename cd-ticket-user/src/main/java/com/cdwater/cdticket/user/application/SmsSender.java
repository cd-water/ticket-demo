package com.cdwater.cdticket.user.application;

public interface SmsSender {
    void send(String phone, String code);
}

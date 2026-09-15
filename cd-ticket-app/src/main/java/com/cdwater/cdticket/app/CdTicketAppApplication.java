package com.cdwater.cdticket.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CdTicketAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdTicketAppApplication.class, args);
    }

}

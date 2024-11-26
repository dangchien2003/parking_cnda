package com.parking.ticket_service;

import com.parking.ticket_service.utils.TimeUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class TicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
        System.out.println(String.valueOf(TimeUtils.timeToLong("00:00:00 26/11/2024", "HH:mm:ss dd/MM/yyyy")));
        System.out.println(String.valueOf(TimeUtils.timeToLong("23:59:59 28/11/2024", "HH:mm:ss dd/MM/yyyy")));
    }

}

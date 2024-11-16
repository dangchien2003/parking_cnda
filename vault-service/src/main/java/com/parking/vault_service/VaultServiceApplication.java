package com.parking.vault_service;

import com.parking.vault_service.utils.TimeUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VaultServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaultServiceApplication.class, args);
//        System.out.println(TimeUtils.timeToLong("14/11/2024", "dd/MM/yyyy"));
//        System.out.println(TimeUtils.timeToLong("15/11/2024", "dd/MM/yyyy"));
        System.out.println(TimeUtils.timeToLong("16/11/2024", "dd/MM/yyyy"));
    }

}

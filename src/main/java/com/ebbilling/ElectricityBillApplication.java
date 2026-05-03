package com.ebbilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ElectricityBillApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElectricityBillApplication.class, args);
    }
}

package com.ketrams.v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"com.ketrams.v2.entity"})  // Ensure JPA entities are scanned
public class KetramsV2Application {

    public static void main(String[] args) {
        SpringApplication.run(KetramsV2Application.class, args);
    }

}
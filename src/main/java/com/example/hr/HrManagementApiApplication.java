package com.example.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HrManagementApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrManagementApiApplication.class, args);
    }
}

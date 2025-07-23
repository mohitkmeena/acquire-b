package com.acquireindia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AcquireIndiaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcquireIndiaApplication.class, args);
    }
}
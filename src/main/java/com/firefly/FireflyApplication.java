package com.firefly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Firefly III Modernized — Java/Spring Boot Modular Monolith.
 * Entry point for the self-hosted personal finance manager.
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
public class FireflyApplication {

    public static void main(String[] args) {
        SpringApplication.run(FireflyApplication.class, args);
    }
}
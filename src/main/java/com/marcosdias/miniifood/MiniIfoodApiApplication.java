package com.marcosdias.miniifood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MiniIfoodApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniIfoodApiApplication.class, args);
    }

}

package com.example.supercacheevict;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SuperCacheEvictApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuperCacheEvictApplication.class, args);
    }

}

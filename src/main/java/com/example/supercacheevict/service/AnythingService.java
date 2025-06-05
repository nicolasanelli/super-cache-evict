package com.example.supercacheevict.service;

import com.example.supercacheevict.domain.Anything;
import com.example.supercacheevict.util.cache.SuperCacheEvict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AnythingService {


    private static final Logger log = LoggerFactory.getLogger(AnythingService.class);
    private final Random rand = new Random();

    @Cacheable(value = "method-one", key = "#key+'-'+#other")
    public Anything method_one(String key, String other) {

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        return generate(key);
    }

    @Cacheable(value = "method-two", key = "#key")
    public Anything method_two(String key) {

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        return generate(key);
    }

    @SuperCacheEvict(value = "method-one", keyPattern = "#key+'-*'")
    public void method_three(String key) {
    }


    private Anything generate(String key) {
        var value = rand.nextInt(100);
        return new Anything(key, value);
    }
}

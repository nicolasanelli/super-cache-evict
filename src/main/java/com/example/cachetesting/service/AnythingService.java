package com.example.cachetesting.web.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AnythingService {


    private final Random rand = new Random();

    public Anything method_one(String key) {

        var value = rand.nextInt(100);

        return new Anything(key, value);
    }


}

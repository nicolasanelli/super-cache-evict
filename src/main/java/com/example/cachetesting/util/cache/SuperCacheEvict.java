package com.example.cachetesting.util.cache;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SuperCacheEviction {
    String value();      // Nome do cache (ex: "cache1")
    String keyPattern(); // Ex: "user::*" (coringa com *)
}
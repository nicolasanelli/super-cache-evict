package com.example.supercacheevict.util.cache;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SuperCacheEvict {
    String value();      // Cache name (ex: "cache1")
    String keyPattern(); // Ex: "#user + '*'" (wildcard with *)
}
package com.example.supercacheevict.controllers;


import com.example.supercacheevict.domain.Anything;
import com.example.supercacheevict.service.AnythingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/anything")
public class AnythingController {

    private final AnythingService anythingService;

    public AnythingController(AnythingService anythingService) {
        this.anythingService = anythingService;
    }

    @GetMapping("one")
    public Anything one(@RequestParam(value = "key") String key) {
        return anythingService.method_one(key, key);
    }

    @GetMapping("two")
    public Anything two(@RequestParam(value = "key") String key) {
        return anythingService.method_two(key);
    }

    @GetMapping("three")
    public void three(@RequestParam(value = "key") String key) {
        anythingService.method_three(key);
    }
}

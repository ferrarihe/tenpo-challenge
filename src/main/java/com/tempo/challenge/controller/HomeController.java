package com.tempo.challenge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("")
public class HomeController {

    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("pong");
    }

}

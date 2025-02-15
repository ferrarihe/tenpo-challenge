package com.tempo.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("")
@Tag(name = "Home Controller", description = "API base")
public class HomeController {

    @GetMapping("/ping")
    @Operation(summary = "Ping del servicio", description = "Verifica si la API está activa")
    public Mono<String> ping() {
        return Mono.just("pong");
    }

}

package com.tempo.challenge.controller;

import com.tempo.challenge.dto.DataSumRequest;
import com.tempo.challenge.service.impl.CalculatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/calculator")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }


    @PostMapping("/calculate")
    public Mono<ResponseEntity<Double>> calcular(@RequestBody DataSumRequest request) {
        return calculatorService.sumWithPercentage(request.getFirst() , request.getSecond())
                .map(result -> ResponseEntity.ok(result))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

}

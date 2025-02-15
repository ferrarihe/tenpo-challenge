package com.tempo.challenge.service;

import reactor.core.publisher.Mono;

public interface ICalculatorService {
    Mono<Double> sumWithPercentage(Double first, Double second);
}
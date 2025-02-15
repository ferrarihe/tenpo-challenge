package com.tempo.challenge.service.impl;

import com.tempo.challenge.client.PorcentageClient;
import com.tempo.challenge.service.ICalculatorService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CalculatorService implements ICalculatorService {

    private final PorcentageClient porcentajeClient;

    public CalculatorService(PorcentageClient porcentajeClient) {
        this.porcentajeClient = porcentajeClient;
    }

    @Override
    public Mono<Double> sumWithPercentage(Double first, Double second) {
        double sum = first + second;
        return porcentajeClient.getPorcentage()
                .map(porcentage -> sum + (sum * porcentage / 100));
    }

}
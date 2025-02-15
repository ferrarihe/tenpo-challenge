package com.tempo.challenge.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PorcentageClient {

    private static final double PORCENTAGE_DEFAULT = 10.0;
    private final WebClient webClient;

    public PorcentageClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://mock-service.com").build();
    }

    public Mono<Double> getPorcentage() {
        return webClient.get()
                .uri("/porcentaje")
                .retrieve()
                .bodyToMono(Double.class)
                .onErrorResume(e -> Mono.just(PORCENTAGE_DEFAULT));
    }

}
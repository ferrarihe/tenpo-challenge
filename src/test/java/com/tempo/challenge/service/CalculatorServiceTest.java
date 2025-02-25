package com.tempo.challenge.service;

import com.tempo.challenge.client.PorcentageClient;
import com.tempo.challenge.service.impl.CalculatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CalculatorServiceTest {

    @Mock
    private PorcentageClient porcentajeClient;

    @InjectMocks
    private CalculatorService calculatorService;

    @Test
    void testCalculoCorrecto() {
        double first = 5.0;
        double second = 5.0;
        double porcentage = 10.0;
        when(porcentajeClient.getPorcentage()).thenReturn(Mono.just(porcentage));
        StepVerifier.create(calculatorService.sumWithPercentage(first, second))
                .expectNext(11.0)
                .verifyComplete();
        verify(porcentajeClient).getPorcentage();
    }

    @Test
    void testServicioExternoFalla() {
        double first = 5;
        double second = 5;
        when(porcentajeClient.getPorcentage()).thenReturn(Mono.error(new RuntimeException("Error en el servicio")));
        StepVerifier.create(calculatorService.sumWithPercentage (first, second))
                .expectError(RuntimeException.class)
                .verify();
    }

}
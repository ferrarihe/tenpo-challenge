package com.tempo.challenge.exception;

import com.tempo.challenge.client.PorcentageClient;
import com.tempo.challenge.service.impl.CalculatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {


    @Mock
    private PorcentageClient porcentajeClient;

    @InjectMocks
    private CalculatorService calculatorService;

    @Test
    void testError400BadRequest() {
        when(porcentajeClient.getPorcentage() ).thenReturn(Mono.error(new IllegalArgumentException("Bad Request")));
        StepVerifier.create(calculatorService.sumWithPercentage(0d, 0d))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testError500InternalServerError() {
        when(porcentajeClient.getPorcentage() ).thenReturn(Mono.error(new RuntimeException("Internal Server Error")));
        StepVerifier.create(calculatorService.sumWithPercentage(5d, 5d))
                .expectError(RuntimeException.class)
                .verify();
    }
}


package com.tempo.challenge.controller;

import com.tempo.challenge.dto.DataSumRequest;
import com.tempo.challenge.service.impl.CalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/calculator")
@Tag(name = "Calculator Controller", description = "API de Calculos")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }


    /**
     * Realiza la suma de dos números y aplica un porcentaje adicional obtenido de un servicio externo.
     *
     * @param request Objeto que contiene los dos números a sumar.
     * @return Mono con la respuesta HTTP que contiene el resultado de la operación o una respuesta de error.
     */
    @PostMapping("/calculate")
    @Operation(summary = "Suma con porcentaje", description = "Servicio que suma dos números y aplica un porcentaje adicional\n" +
            "obtenido de un servicio externo")
    public Mono<ResponseEntity<Double>> calcular(@RequestBody DataSumRequest request) {
        return calculatorService.sumWithPercentage(request.getFirst() , request.getSecond())
                .map(result -> ResponseEntity.ok(result))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

}

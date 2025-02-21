package com.tempo.challenge.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Manejar IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Devuelve una respuesta con el mensaje y el código de estado
        return Mono.just(ResponseEntity.badRequest().body("Invalid request parameter: " + ex.getMessage()));
    }

    // Manejar ResponseStatusException (excepciones de estado HTTP)
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<String>> handleResponseStatusException(ResponseStatusException ex) {
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(ex.getReason()));
    }

    // Manejo de otras excepciones generales
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleException(Exception ex) {
        return Mono.just(ResponseEntity.status(500).body("Internal server error: " + ex.getMessage()));
    }
}


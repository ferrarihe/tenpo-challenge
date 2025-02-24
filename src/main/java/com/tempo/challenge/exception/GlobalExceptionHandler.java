package com.tempo.challenge.exception;

import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisException;
import io.r2dbc.spi.R2dbcBadGrammarException;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import io.r2dbc.spi.R2dbcTimeoutException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.r2dbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@ComponentScan(basePackages = "com.tempo.challenge")
public class GlobalExceptionHandler {


    /********************************************************************************************/
    /*********************************** REDIS RATE LIMITING *************************************/
    /********************************************************************************************/

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleRateLimitExceeded(RateLimitExceededException ex) {
        String errorMessage = ex.getMessage();
        return buildErrorResponse("Too Many Requests", errorMessage, HttpStatus.TOO_MANY_REQUESTS);
    }


    /********************************************************************************************/
    /*********************************** EXCEPTIONS GENERIC *************************************/
    /********************************************************************************************/

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return buildErrorResponse("Error inesperado", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse("Parametros invalidos en la peticion", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDataAccess(DataAccessException ex) {
        return buildErrorResponse("Error de acceso a los datos.", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return buildErrorResponse("Error inesperado.", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        return buildErrorResponse("Error inesperado al generar la repuesta.", ex.getMessage(), HttpStatus.BAD_REQUEST);

    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String error, String details, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("details", details);
        response.put("error", error);
        return new ResponseEntity<>(response, status);
    }


    /********************************************************************************************/
    /*********************************** EXCEPTIONS WEB CLIENT **********************************/
    /********************************************************************************************/


    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleWebClientResponseException(WebClientResponseException ex) {
        return buildErrorResponse("Error en la API externa", ex.getMessage(), HttpStatus.BAD_REQUEST);

    }

    /********************************************************************************************/
    /*********************************** EXCEPTIONS POSTGRESQL **********************************/
    /********************************************************************************************/


    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<Map<String, String>> handleBadSqlGrammar(BadSqlGrammarException ex) {
        return buildErrorResponse("Error de sintaxis SQL.", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(R2dbcBadGrammarException.class)
    public ResponseEntity<Map<String, String>> handleR2dbcBadGrammar(R2dbcBadGrammarException ex) {
        return buildErrorResponse("Declaracion SQL Invalido.", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(R2dbcDataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(R2dbcDataIntegrityViolationException ex) {
        return buildErrorResponse("Violación de la integridad de los datos.", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(R2dbcTimeoutException.class)
    public ResponseEntity<Map<String, String>> handleTimeout(R2dbcTimeoutException ex) {
        return buildErrorResponse("Database Timeout", "La transaccion tardo demasiado tiempo.", HttpStatus.REQUEST_TIMEOUT);
    }


    /********************************************************************************************/
    /************************************* EXCEPTIONS REDIS *************************************/
    /********************************************************************************************/


    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<Map<String, String>> handleRedisConnectionFailure(RedisConnectionFailureException ex) {
        return buildErrorResponse("Error de conexión con Redis", "No se pudo conectar al servidor Redis.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(RedisCommandTimeoutException.class)
    public ResponseEntity<Map<String, String>> handleRedisTimeout(RedisCommandTimeoutException ex) {
        return buildErrorResponse("Tiempo de espera agotado en Redis", "El comando en Redis tardó demasiado en ejecutarse.", HttpStatus.GATEWAY_TIMEOUT);
    }

    @ExceptionHandler(RedisException.class)
    public ResponseEntity<Map<String, String>> handleRedisGenericError(RedisException ex) {
        return buildErrorResponse("Error en Redis", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
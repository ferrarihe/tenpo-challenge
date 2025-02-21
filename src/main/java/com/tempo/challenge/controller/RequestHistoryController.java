package com.tempo.challenge.controller;

import com.tempo.challenge.dto.RequestHistoryResponse;
import com.tempo.challenge.service.IRequestHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/request-history")
@Tag(name = "RequestHistory Controller", description = "API de registo de peticiones")
public class RequestHistoryController {

    private final IRequestHistoryService requestHistoryService;

    public RequestHistoryController(IRequestHistoryService requestHistoryService) {
        this.requestHistoryService = requestHistoryService;
    }

    @GetMapping
    @Operation(summary = "Devuelve Historial de peticiones", description = "Servicio que devuelve Historial de Peticiones recibidas en la API.")
    public Mono<Page<RequestHistoryResponse>> getCallHistory(@RequestParam int page, @RequestParam int size) {
        if (size < 1) {
            size = 1;
        }
        Pageable pageable = PageRequest.of(page, size);
        return requestHistoryService.getRequestHistory(pageable);
    }

}

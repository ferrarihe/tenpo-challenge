package com.tempo.challenge.interceptor;

import com.tempo.challenge.entities.RequestHistory;
import com.tempo.challenge.repository.RequestHistoryRepository;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;


@Component
public class RequestHistoryFilter implements WebFilter {

    private final RequestHistoryRepository requestHistoryRepository;

    public RequestHistoryFilter(RequestHistoryRepository requestHistoryRepository) {
        this.requestHistoryRepository = requestHistoryRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (FilterUtils.isSwaggerPath(exchange)) {
            return chain.filter(exchange);
        }

        String endpoint = exchange.getRequest().getURI().toString();
        String parameters = exchange.getRequest().getQueryParams().toString();

        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return super.writeWith(body).doOnSuccess(aVoid -> {
                    saveRequestHistory(exchange, endpoint, parameters, null);
                });
            }
        };

        return chain.filter(exchange.mutate().response(responseDecorator).build())
                .doOnError(throwable -> {
                    saveRequestHistory(exchange, endpoint, parameters, throwable.getMessage());
                });
    }

    private void saveRequestHistory(ServerWebExchange exchange, String endpoint, String parameters, String error) {
        RequestHistory record = RequestHistory.builder()
                .timestamp(Instant.now())
                .endpoint(endpoint)
                .parameters(parameters)
                .response(exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().toString() : "Unknown")
                .error(error)
                .build();

        requestHistoryRepository.save(record).subscribe();
    }
}
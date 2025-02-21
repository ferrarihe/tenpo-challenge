package com.tempo.challenge.interceptor;

import com.tempo.challenge.entities.RequestHistory;
import com.tempo.challenge.repository.RequestHistoryRepository;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Collectors;

// @Component implements WebFilter
public class RequestHistoryFilter  {

    private final RequestHistoryRepository requestHistoryRepository;

    public RequestHistoryFilter(RequestHistoryRepository requestHistoryRepository) {
        this.requestHistoryRepository = requestHistoryRepository;
    }

    // @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestUri = request.getURI().toString();

        // Capturar Request Body sin bloquear
        return DataBufferUtils.join(request.getBody())
                .flatMap(requestBuffer -> {
                    String requestBody = extractBody(requestBuffer);

                    // Decorar la respuesta para capturar el response body
                    ServerHttpResponse originalResponse = exchange.getResponse();
                    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                        @Override
                        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                            if (body instanceof Flux) {
                                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                                return super.writeWith(fluxBody.doOnNext(dataBuffer -> {
                                    String responseBody = extractBody(dataBuffer);
                                    HttpStatusCode responseStatus = getStatusCode();

                                    // Guardar la solicitud y respuesta en base de datos de forma asíncrona
                                    RequestHistory history = RequestHistory.builder()
                                            .timestamp(Instant.now())
                                            .endpoint(requestUri)
                                            .parameters(requestBody)
                                            .response(responseBody)
                                            .error(responseStatus != null && responseStatus.isError() ? responseBody : null)
                                            .build();

                                    requestHistoryRepository.save(history).subscribe();
                                }));
                            }
                            return super.writeWith(body);
                        }
                    };

                    return chain.filter(exchange.mutate().response(decoratedResponse).build());
                });
    }

    private static String extractBody(DataBuffer buffer) {
        byte[] bytes = new byte[buffer.readableByteCount()];
        buffer.read(bytes);
        DataBufferUtils.release(buffer);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}

package com.tempo.challenge.interceptor;

import com.tempo.challenge.entities.RequestHistory;
import com.tempo.challenge.repository.RequestHistoryRepository;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;


@Component
public class RequestHistoryFilter implements WebFilter {

    private final RequestHistoryRepository requestHistoryRepository;

    public RequestHistoryFilter(RequestHistoryRepository requestHistoryRepository) {
        this.requestHistoryRepository = requestHistoryRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestUri = request.getURI().toString();

        // Solo procesar el cuerpo de la solicitud sin bloquear el flujo
        return chain.filter(exchange)
                .doOnTerminate(() -> {
                    // Recoger el cuerpo de la solicitud y registrarlo en el historial
                    exchange.getRequest().getBody()
                            .collectList()  // Recoger el cuerpo en una lista de DataBuffers
                            .flatMap(requestBuffers -> {
                                String requestBody = extractBody(requestBuffers);

                                // Crear el historial de la solicitud
                                RequestHistory history = RequestHistory.builder()
                                        .timestamp(Instant.now())
                                        .endpoint(requestUri)
                                        .parameters(requestBody)
                                        .response("No se registró el cuerpo de la respuesta") // Mensaje indicativo
                                        .build();

                                // Log para verificar la creación del historial
                                System.out.println("Historial creado: " + history);

                                // Guardar el historial de manera asíncrona sin bloquear
                                return requestHistoryRepository.save(history)
                                        .doOnSuccess(savedHistory -> {
                                            // Log cuando el historial se guarda correctamente
                                            System.out.println("Historial guardado: " + savedHistory);
                                        })
                                        .doOnError(error -> {
                                            // Log cuando ocurre un error al guardar
                                            System.err.println("Error al guardar historial: " + error.getMessage());
                                        });
                            }).subscribe();  // Ejecutar el registro sin bloquear el flujo principal
                });
    }

    private static String extractBody(List<DataBuffer> buffers) {
        StringBuilder body = new StringBuilder();
        buffers.forEach(buffer -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            DataBufferUtils.release(buffer);
            body.append(new String(bytes, StandardCharsets.UTF_8));
        });
        return body.toString();
    }
}

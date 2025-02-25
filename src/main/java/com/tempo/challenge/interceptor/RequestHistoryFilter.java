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

    /**
     * Filtro que intercepta todas las solicitudes HTTP para registrar su historial,
     * excluyendo las rutas de Swagger.
     *
     * @param exchange Intercambio de la solicitud y respuesta del servidor.
     * @param chain    Cadena de filtros a aplicar.
     * @return Mono que representa la ejecución del filtro.
     */
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

    /**
     * Metodo private que persiste un registro en el historial de solicitudes.
     *
     * @param exchange  Intercambio de la solicitud y respuesta.
     * @param endpoint  URL del endpoint accedido.
     * @param parameters Parámetros de la solicitud.
     * @param error     Mensaje de error en caso de fallo (puede ser null).
     */
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
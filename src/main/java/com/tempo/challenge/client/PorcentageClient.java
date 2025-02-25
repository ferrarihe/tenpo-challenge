package com.tempo.challenge.client;

import com.tempo.challenge.client.cache.CacheItem;
import com.tempo.challenge.config.PorcentageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PorcentageClient {

    private static final Logger logger = LoggerFactory.getLogger(PorcentageClient.class);
    private final PorcentageConfig porcentageConfig;
    private final WebClient webClient;
    private final ConcurrentHashMap<String, CacheItem> cache = new ConcurrentHashMap<>();

    public PorcentageClient(WebClient.Builder webClientBuilder, PorcentageConfig porcentageConfig) {
        this.webClient = webClientBuilder.baseUrl("http://mock-service.com").build();
        this.porcentageConfig = porcentageConfig;
    }

    /**
     * Obtiene un porcentaje desde un servicio externo con soporte de caché.
     * Si el servicio externo falla, intenta obtener el valor desde caché o usa un valor por defecto.
     * Se aplican hasta porcentageConfig.getRetryAttempts() reintentos o retorna porcentageConfig.getDefaultValue()
     *
     * @return Mono con el porcentaje obtenido.
     */
    public Mono<Double> getPorcentage() {
        String cacheKey = "porcentage";
        CacheItem cacheItem = cache.get(cacheKey);

        if (cacheItem != null && !cacheItem.isExpired()) {
            logger.info("Obteniendo porcentaje desde caché: {}%", cacheItem.getPorcentage());
            return Mono.just(cacheItem.getPorcentage());
        }

        logger.info("Llamando API externa para obtener el porcentaje...");

        return webClient.get()
                .uri("/porcentaje")
                .retrieve()
                .bodyToMono(Double.class)
                .retryWhen(Retry.fixedDelay(porcentageConfig.getRetryAttempts(), Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal ->
                                logger.warn("Reintento #{} fallido. Motivo: {}",
                                        retrySignal.totalRetries() + 1,
                                        retrySignal.failure().getMessage()
                                )
                        )
                )
                .doOnNext(porcentage -> {
                    cache.put(cacheKey, new CacheItem(porcentage));
                    logger.info("Nuevo porcentaje obtenido y guardado en caché: {}%", porcentage);
                })
                .onErrorResume(e -> {
                    if (cacheItem != null) {
                        logger.warn("Servicio externo fallido, utilizando caché...");
                        return Mono.just(cacheItem.getPorcentage());
                    }
                    logger.error("No se pudo obtener el porcentaje, devolviendo valor por defecto.");
                    return Mono.just(porcentageConfig.getDefaultValue());
                });
    }

}
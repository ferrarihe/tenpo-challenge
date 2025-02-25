package com.tempo.challenge.interceptor;
import com.tempo.challenge.exception.RateLimitExceededException;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
public class RedisRateLimiterFilter implements WebFilter {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public RedisRateLimiterFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Filtro que implementa un limitador de tasa basado en Redis para restringir
     * el número de solicitudes permitidas desde una misma IP en un tiempo determinado.
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

        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        String key = "rate_limit:" + ip;

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        redisTemplate.expire(key, WINDOW).subscribe();
                    }

                    // Obtener el tiempo restante de expiración
                    return redisTemplate.getExpire(key)
                            .flatMap(expireTime -> {
                                if (expireTime == null) {
                                    expireTime = Duration.ZERO;
                                }
                                long remainingTimeInSeconds = expireTime.getSeconds();

                                if (count > LIMIT) {
                                    String message = String.format(
                                            "Demasiados intentos, por favor espere %d segundos. Intentos realizados: %d",
                                            remainingTimeInSeconds, count);
                                    throw new RateLimitExceededException(message);
                                }

                                return chain.filter(exchange);
                            });
                });
    }
}

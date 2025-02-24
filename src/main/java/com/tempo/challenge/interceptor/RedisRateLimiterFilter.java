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
import java.time.Instant;

@Component
public class RedisRateLimiterFilter implements WebFilter {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public RedisRateLimiterFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // Excluir rutas de Swagger y OpenAPI
        if (path.contains("/swagger-ui/") || path.contains("/v3/api-docs") || path.contains("/webjars/")) {
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

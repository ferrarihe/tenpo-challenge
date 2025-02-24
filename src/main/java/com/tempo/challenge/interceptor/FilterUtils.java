package com.tempo.challenge.interceptor;

import org.springframework.web.server.ServerWebExchange;

public class FilterUtils {
    private FilterUtils() {}

    public static boolean isSwaggerPath(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        return path.contains("/swagger-ui/") ||
                path.contains("/v3/api-docs") ||
                path.contains("/webjars/") ||
                path.contains("swagger-ui.html");
    }
}


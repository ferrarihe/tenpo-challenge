package com.tempo.challenge.client.cache;

import java.time.Duration;
import java.time.Instant;

public class CacheItem {

    private final Duration cacheDuration = Duration.ofMinutes(30);
    private final double porcentage;
    private final Instant timestamp;

    public CacheItem(double porcentage) {
        this.porcentage = porcentage;
        this.timestamp = Instant.now();
    }

    public double getPorcentage() {
        return porcentage;
    }

    public boolean isExpired() {
        return Duration.between(timestamp, Instant.now()).compareTo(cacheDuration) > 0;

    }
}

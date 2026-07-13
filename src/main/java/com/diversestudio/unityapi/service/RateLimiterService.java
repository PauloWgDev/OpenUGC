package com.diversestudio.unityapi.service;


import io.github.bucket4j.*;
import io.github.bucket4j.local.LocalBucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> newBucket());
    }

    private  Bucket newBucket() {
        Refill refill = Refill.greedy(5, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(5, refill);
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(limit)
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

package com.diversestudio.openugc.service;


import com.diversestudio.openugc.exception.RateLimitedException;
import io.github.bucket4j.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> newBucket());
    }

    /**
     * Takes one request from the bucket of {@code key}.
     *
     * @throws RateLimitedException if the bucket is empty (429), with the seconds until the next request is allowed
     */
    public void consume(String key, String message) {
        ConsumptionProbe probe = resolveBucket(key).tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            // Round up, so the client never retries a moment too early.
            long seconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill() + 999_999_999L);
            throw new RateLimitedException(message, Math.max(1, seconds));
        }
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

package com.diversestudio.openugc.exception;

/**
 * Too many requests from the same client. Mapped to 429 with a {@code Retry-After} header.
 */
public class RateLimitedException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitedException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /** Seconds until the client can try again (at least 1). */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

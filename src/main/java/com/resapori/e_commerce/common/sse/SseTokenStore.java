package com.resapori.e_commerce.common.sse;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Short-lived, auto-expiring token store for SSE authentication.
 *
 * <p>Because browser {@code EventSource} cannot set custom HTTP headers, JWT auth is
 * implemented via a one-time token: the admin calls {@code GET /api/sse/token} with
 * a valid JWT, receives a random UUID that is valid for 30 seconds, and then opens
 * the SSE stream with {@code ?token=<uuid>}.
 *
 * <p>Uses Caffeine (a transitive Spring Boot dependency) for automatic eviction instead
 * of a manual cleanup thread, with a hard limit of 500 concurrent pending tokens to
 * prevent unbounded growth in case tokens are requested but never consumed.
 */
@Component
public class SseTokenStore {

    public record SseTokenEntry(String userId, String username) {}

    private final Cache<UUID, SseTokenEntry> tokenStore = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(500)
            .build();

    /** Stores a new token and returns the generated UUID. */
    public UUID issue(String userId, String username) {
        UUID token = UUID.randomUUID();
        tokenStore.put(token, new SseTokenEntry(userId, username));
        return token;
    }

    /**
     * Validates and immediately invalidates the token (one-time use).
     *
     * @return the associated entry, or {@code null} if not found / expired.
     */
    public SseTokenEntry consume(UUID token) {
        SseTokenEntry entry = tokenStore.getIfPresent(token);
        if (entry != null) {
            tokenStore.invalidate(token);
        }
        return entry;
    }
}

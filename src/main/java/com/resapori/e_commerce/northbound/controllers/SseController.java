package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.common.security.AuthUtil;
import com.resapori.e_commerce.common.sse.SseEmitterRegistry;
import com.resapori.e_commerce.common.sse.SseTokenStore;
import com.resapori.e_commerce.southbound.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * SSE endpoints for real-time admin notifications.
 *
 * <h3>Auth flow</h3>
 * <ol>
 *   <li>Admin calls {@code GET /api/sse/token} with a valid JWT → receives a one-time UUID
 *       token valid for 30 s.</li>
 *   <li>Admin's browser opens {@code GET /api/sse/orders?token=<uuid>} (no JWT needed —
 *       browsers cannot set custom headers on EventSource).</li>
 *   <li>The token is validated against {@link SseTokenStore} and immediately invalidated
 *       (one-time use) to prevent replay attacks.</li>
 * </ol>
 *
 * <h3>Nginx / reverse-proxy notes</h3>
 * The response header {@code X-Accel-Buffering: no} disables Nginx proxy buffering so that
 * SSE events are flushed to the client immediately rather than held in a buffer.
 */
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterRegistry registry;
    private final SseTokenStore tokenStore;
    private final AuthUtil authUtil;

    /**
     * Issues a short-lived one-time token that the client can use to open the SSE stream.
     * Requires an authenticated admin or cashier session (Bearer JWT).
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> issueToken() {
        User user = authUtil.getAuthenticatedUser();
        String userId   = user != null ? user.getId().toString() : "unknown";
        String username = user != null ? user.getEmail() : "unknown";
        UUID token = tokenStore.issue(userId, username);
        return ResponseEntity.ok(Map.of("token", token.toString()));
    }

    /**
     * Opens a persistent SSE stream for the admin panel.
     * Auth is performed via the one-time {@code token} query parameter (see class javadoc).
     * This endpoint is {@code permitAll()} in {@link com.resapori.e_commerce.common.config.SecurityConfig}
     * because browsers cannot set Authorization headers on EventSource connections.
     */
    @GetMapping(value = "/orders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrders(
            @RequestParam String token,
            HttpServletResponse response) {

        // Disable Nginx buffering so events are sent immediately
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        // Validate and consume the one-time token
        UUID tokenUuid;
        try {
            tokenUuid = UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            log.warn("SSE connection rejected: malformed token '{}'", token);
            throw new SecurityException("Invalid SSE token");
        }

        SseTokenStore.SseTokenEntry entry = tokenStore.consume(tokenUuid);
        if (entry == null) {
            log.warn("SSE connection rejected: token not found or expired '{}'", token);
            throw new SecurityException("SSE token expired or invalid");
        }

        String emitterId = entry.userId() + "-" + UUID.randomUUID();
        log.info("SSE client connected: user={}, emitterId={}", entry.username(), emitterId);

        SseEmitter emitter = registry.register(emitterId);

        // Send an immediate "connected" event so the client knows the stream is live
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("status", "connected", "emitterId", emitterId)));
        } catch (IOException e) {
            log.warn("Failed to send initial 'connected' event to {}: {}", emitterId, e.getMessage());
        }

        return emitter;
    }
}

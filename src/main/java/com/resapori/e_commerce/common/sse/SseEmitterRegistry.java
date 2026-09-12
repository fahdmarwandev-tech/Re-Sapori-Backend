package com.resapori.e_commerce.common.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry of all active SSE emitters (one per connected admin browser tab).
 *
 * <p>Every emitter is registered with {@code onCompletion}, {@code onTimeout}, and
 * {@code onError} hooks so that dead connections are automatically evicted from the map,
 * preventing memory leaks and repeated {@link IOException}s on broadcast.
 *
 * <p>A {@link #sendHeartbeat()} method runs every 25 seconds to keep connections alive
 * through reverse proxies (Nginx, AWS ALB, Cloudflare) that terminate idle HTTP streams
 * after 60–100 seconds.
 */
@Slf4j
@Component
public class SseEmitterRegistry {

    /** Injected from {@code sse.emitter-timeout-ms} in application.yml (default 5 min). */
    @Value("${sse.emitter-timeout-ms:300000}")
    private long emitterTimeoutMs;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Creates, registers, and returns a new {@link SseEmitter} for the given {@code emitterId}.
     * All three lifecycle hooks remove the emitter from the map on completion, timeout, or error.
     */
    public SseEmitter register(String emitterId) {
        SseEmitter emitter = new SseEmitter(emitterTimeoutMs);

        Runnable cleanup = () -> {
            emitters.remove(emitterId);
            log.debug("SSE emitter removed: {}", emitterId);
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        emitters.put(emitterId, emitter);
        log.debug("SSE emitter registered: {} (total={})", emitterId, emitters.size());
        return emitter;
    }

    /**
     * Broadcasts a named SSE event with the given JSON payload to all connected emitters.
     * Stale emitters that fail to send are removed immediately.
     */
    public void broadcast(String eventName, Object data) {
        if (emitters.isEmpty()) {
            return;
        }
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name(eventName)
                .data(data);

        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(event);
            } catch (IOException | IllegalStateException ex) {
                log.warn("Failed to send SSE to emitter {}: {}. Removing.", id, ex.getMessage());
                emitters.remove(id);
                emitter.completeWithError(ex);
            }
        });
    }

    /**
     * Sends a lightweight SSE comment ping every 25 seconds to keep connections alive
     * through proxies that would otherwise drop idle streams at 60–100 s.
     * The {@code X-Accel-Buffering: no} header set in {@code SseController} ensures
     * Nginx flushes these comments immediately without buffering.
     */
    @Scheduled(fixedRate = 25_000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }
        SseEmitter.SseEventBuilder ping = SseEmitter.event().comment("ping");
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(ping);
            } catch (IOException | IllegalStateException ex) {
                log.debug("Heartbeat failed for emitter {}: {}. Removing.", id, ex.getMessage());
                emitters.remove(id);
            }
        });
        log.trace("SSE heartbeat sent to {} client(s)", emitters.size());
    }

    /** Returns the number of currently connected admin clients. */
    public int connectedCount() {
        return emitters.size();
    }
}

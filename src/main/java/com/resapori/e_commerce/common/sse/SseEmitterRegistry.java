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

    public record SseClient(SseEmitter emitter, java.util.UUID branchId, boolean isAdmin) {}

    private final Map<String, SseClient> clients = new ConcurrentHashMap<>();

    /**
     * Creates, registers, and returns a new {@link SseEmitter} for the given {@code emitterId}
     * with associated branch and admin status for scoped broadcasts.
     */
    public SseEmitter register(String emitterId, java.util.UUID branchId, boolean isAdmin) {
        SseEmitter emitter = new SseEmitter(emitterTimeoutMs);

        Runnable cleanup = () -> {
            clients.remove(emitterId);
            log.debug("SSE emitter removed: {}", emitterId);
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        clients.put(emitterId, new SseClient(emitter, branchId, isAdmin));
        log.debug("SSE emitter registered: {} (branchId={}, isAdmin={}, total={})",
                emitterId, branchId, isAdmin, clients.size());
        return emitter;
    }

    /**
     * Broadcasts a named SSE event with the given JSON payload to connected emitters.
     * Cashiers only receive events for their assigned branch, while Admins receive all.
     */
    public void broadcast(String eventName, Object data) {
        if (clients.isEmpty()) {
            return;
        }

        java.util.UUID orderBranchId = null;
        if (data instanceof com.resapori.e_commerce.northbound.dto.order.OrderResponse orderRes) {
            orderBranchId = orderRes.getBranchId();
        }

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name(eventName)
                .data(data);

        for (Map.Entry<String, SseClient> entry : clients.entrySet()) {
            String id = entry.getKey();
            SseClient client = entry.getValue();

            // Branch filtering: non-admin cashiers only receive orders belonging to their branch
            if (!client.isAdmin() && client.branchId() != null && orderBranchId != null) {
                if (!client.branchId().equals(orderBranchId)) {
                    continue; // Skip: order belongs to another branch
                }
            }

            try {
                client.emitter().send(event);
            } catch (IOException | IllegalStateException ex) {
                log.warn("Failed to send SSE to emitter {}: {}. Removing.", id, ex.getMessage());
                clients.remove(id);
                client.emitter().completeWithError(ex);
            }
        }
    }

    /**
     * Sends a lightweight SSE comment ping every 25 seconds to keep connections alive
     * through proxies that would otherwise drop idle streams at 60–100 s.
     */
    @Scheduled(fixedRate = 25_000)
    public void sendHeartbeat() {
        if (clients.isEmpty()) {
            return;
        }
        SseEmitter.SseEventBuilder ping = SseEmitter.event().comment("ping");
        clients.forEach((id, client) -> {
            try {
                client.emitter().send(ping);
            } catch (IOException | IllegalStateException ex) {
                log.debug("Heartbeat failed for emitter {}: {}. Removing.", id, ex.getMessage());
                clients.remove(id);
            }
        });
        log.trace("SSE heartbeat sent to {} client(s)", clients.size());
    }

    /** Returns the number of currently connected admin/staff clients. */
    public int connectedCount() {
        return clients.size();
    }
}

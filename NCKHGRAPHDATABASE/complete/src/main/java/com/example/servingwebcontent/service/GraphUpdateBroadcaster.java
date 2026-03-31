package com.example.servingwebcontent.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple SSE broadcaster for near real-time UI updates.
 * We only push "something changed" + a small payload; the UI decides how to refresh.
 */
@Service
public class GraphUpdateBroadcaster {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        // 0L = no timeout. If you prefer, set e.g. 30 minutes.
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // Initial hello/heartbeat so the client knows it's connected.
        try {
            emitter.send(SseEmitter.event()
                    .name("hello")
                    .data(Map.of("ts", Instant.now().toString()), MediaType.APPLICATION_JSON));
        } catch (IOException ignored) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void publish(String eventName, Map<String, Object> payload) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName == null ? "update" : eventName)
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}


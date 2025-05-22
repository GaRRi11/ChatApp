package com.gary.ChatApp.config;

import com.gary.ChatApp.domain.service.userPresenceService.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Slf4j
@Component
@RequiredArgsConstructor

/*
*
* Listens for WebSocket lifecycle events: connection and disconnection.

Helps keep track of who is currently active, which is crucial for
*  chat presence features.
 *
* */
public class PresenceWebSocketEventListener {

    private final UserPresenceService presenceService;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        Long userId = extractUserId(event);
        if (userId != null) {
            presenceService.setOnline(userId);
        }
        log.info("User {} connected", userId);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Long userId = extractUserId(event);
        if (userId != null) {
            presenceService.setOffline(userId);
        }
        log.info("User {} disconnected", userId);
    }

    private Long extractUserId(AbstractSubProtocolEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String idStr = accessor.getFirstNativeHeader("user-id");
        if (idStr == null) {
            log.warn("Missing user-id header in WebSocket connection.");
        }
        try {
            return idStr != null ? Long.parseLong(idStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

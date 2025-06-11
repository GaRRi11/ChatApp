package com.gary.infrastructure.websocket;

import com.gary.domain.service.presence.UserPresenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.WebSocketHandler;

@Slf4j
public class UserPresenceWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

    private static final String USER_ID_ATTR = "user-id";

    private final UserPresenceService userPresenceService;

    public UserPresenceWebSocketHandlerDecorator (WebSocketHandler delegate, UserPresenceService userPresenceService) {
        super(delegate);
        this.userPresenceService = userPresenceService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Object userIdObj = session.getAttributes().get(USER_ID_ATTR);
        if (userIdObj instanceof Long userId) {
            userPresenceService.refreshOnlineStatus(userId);
            log.debug("User {} connected and marked online. Session ID: {}, IP: {}",
                    userId,
                    session.getId(),
                    session.getRemoteAddress() != null ? session.getRemoteAddress().getAddress().getHostAddress() : "unknown");
        } else {
            log.warn("Connection established but user-id attribute is missing or invalid. Session ID: {}, IP: {}",
                    session.getId(),
                    session.getRemoteAddress() != null ? session.getRemoteAddress().getAddress().getHostAddress() : "unknown");
        }
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Object userIdObj = session.getAttributes().get(USER_ID_ATTR);
        if (userIdObj instanceof Long userId) {
            userPresenceService.setOffline(userId);
            log.debug("User {} disconnected and marked offline. Session ID: {}, IP: {}",
                    userId,
                    session.getId(),
                    session.getRemoteAddress() != null ? session.getRemoteAddress().getAddress().getHostAddress() : "unknown");
        } else {
            log.warn("Connection closed but user-id attribute is missing or invalid. Session ID: {}, IP: {}",
                    session.getId(),
                    session.getRemoteAddress() != null ? session.getRemoteAddress().getAddress().getHostAddress() : "unknown");
        }
        super.afterConnectionClosed(session, closeStatus);
    }
}


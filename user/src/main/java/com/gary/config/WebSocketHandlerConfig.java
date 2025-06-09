package com.gary.config;

import com.gary.domain.service.presence.UserPresenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.WebSocketHandler;

@Slf4j
public class WebSocketHandlerConfig extends WebSocketHandlerDecorator {

    private static final String USER_ID_ATTR = "user-id";

    private final UserPresenceService userPresenceService;

    public WebSocketHandlerConfig(WebSocketHandler delegate, UserPresenceService userPresenceService) {
        super(delegate);
        this.userPresenceService = userPresenceService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get(USER_ID_ATTR);
        if (userId != null) {
            userPresenceService.refreshOnlineStatus(userId);
            log.debug("User {} connected and marked online", userId);
        } else {
            log.warn("Connection established but user-id attribute is missing");
        }
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Long userId = (Long) session.getAttributes().get(USER_ID_ATTR);
        if (userId != null) {
            userPresenceService.setOffline(userId);
            log.debug("User {} disconnected and marked offline", userId);
        } else {
            log.warn("Connection closed but user-id attribute is missing");
        }
        super.afterConnectionClosed(session, closeStatus);
    }
}


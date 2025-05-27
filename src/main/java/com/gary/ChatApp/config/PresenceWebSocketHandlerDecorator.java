package com.gary.ChatApp.config;

import com.gary.ChatApp.domain.service.userPresenceService.UserPresenceService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.WebSocketHandler;

@RequiredArgsConstructor
public class PresenceWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

    private final UserPresenceService userPresenceService;

    public PresenceWebSocketHandlerDecorator(WebSocketHandler delegate, UserPresenceService userPresenceService) {
        super(delegate);
        this.userPresenceService = userPresenceService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("user-id");
        if (userId != null) {
            userPresenceService.refreshOnlineStatus(userId);
        }
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus closeStatus) throws Exception {
        Long userId = (Long) session.getAttributes().get("user-id");
        if (userId != null) {
            userPresenceService.setOffline(userId);
        }
        super.afterConnectionClosed(session, closeStatus);
    }
}

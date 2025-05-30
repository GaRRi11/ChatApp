package com.gary.ChatApp.infrastructure.websocket;

import com.gary.ChatApp.domain.service.presence.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PingMessageInterceptor implements ChannelInterceptor {

    private static final String USER_ID_ATTR = "user-id";
    private static final String PING_DESTINATION = "/app/ping";

    private final UserPresenceService userPresenceService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        if (SimpMessageType.MESSAGE.equals(accessor.getMessageType())) {
            String destination = accessor.getDestination();
            if (PING_DESTINATION.equals(destination)) {
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    Long userId = (Long) sessionAttributes.get(USER_ID_ATTR);
                    if (userId != null) {
                        userPresenceService.refreshOnlineStatus(userId);
                        log.debug("Refreshed online status for user {}", userId);
                    } else {
                        log.warn("Ping message received but user-id missing in session attributes");
                    }
                }
            }
        }
        return message;
    }
}

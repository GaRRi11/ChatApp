package com.gary.ChatApp.interceptor;

import com.gary.ChatApp.domain.service.userPresenceService.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PingMessageInterceptor implements ChannelInterceptor {

    private final UserPresenceService userPresenceService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (SimpMessageType.MESSAGE.equals(message.getHeaders().get("simpMessageType"))) {
            String destination = (String) message.getHeaders().get("simpDestination");
            if ("/app/ping".equals(destination)) {
                Map<String, Object> sessionAttributes = (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
                if (sessionAttributes != null && sessionAttributes.containsKey("user-id")) {
                    Long userId = (Long) sessionAttributes.get("user-id");
                    userPresenceService.refreshOnlineStatus(userId);
                }
            }
        }
        return message;
    }
}

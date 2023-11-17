package com.gary.ChatApp.config;

import com.gary.ChatApp.service.user.UserService;
import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import com.gary.ChatApp.storage.model.chatmessage.MessageType;
import com.gary.ChatApp.storage.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messageTemplate;
    private final UserService userService;

//    @EventListener
//    public void HandlWebSocketDisconnectListener (
//            SessionDisconnectEvent event
//    ){
//        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        String username = (String) headerAccessor.getSessionAttributes().get("username");
//        if (username != null) {
//            log.info("User disconnected: {}", username);
//            var chatMessage = ChatMessage.builder()
//                    //.messageType(MessageType.LEAVER)
//                    .sender(username)
//                    .build();
//            messageTemplate.convertAndSend("/topic/public", chatMessage);
//        }
//    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            log.info("User connected: {}", username);
            updateUserOnlineStatus(username, true);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            log.info("User disconnected: {}", username);
            updateUserOnlineStatus(username, false);
        }
    }

    private void updateUserOnlineStatus(String username, boolean onlineStatus) {
        User user = userService.findByName(username).orElseThrow();
        userService.setUserOnlineStatus(user, onlineStatus);
    }
}

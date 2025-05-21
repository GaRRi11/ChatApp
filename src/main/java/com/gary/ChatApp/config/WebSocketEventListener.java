//package com.gary.ChatApp.config;
//
//import com.gary.ChatApp.domain.service.user.UserService;
//import com.gary.ChatApp.domain.model.user.User;
//import com.gary.ChatApp.web.security.UserContext;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.event.EventListener;
//import org.springframework.messaging.simp.SimpMessageSendingOperations;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.messaging.SessionConnectedEvent;
//import org.springframework.web.socket.messaging.SessionDisconnectEvent;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class WebSocketEventListener {
//
//    private final UserService userService;
//
//
//    @EventListener
//    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
//        User user = UserContext.getUser();
//        if (user != null) {
//            log.info("User connected: {}", user.getName());
//            updateUserOnlineStatus(user, true);
//        }
//    }
//
//    @EventListener
//    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//        User user = UserContext.getUser();
//        if (user != null) {
//            log.info("User disconnected: {}", user.getName());
//            updateUserOnlineStatus(user, false);
//        }
//    }
//
//    private void updateUserOnlineStatus(User user, boolean onlineStatus) {
//        userService.updateUserOnlineStatus(user,onlineStatus);
//    }
//}

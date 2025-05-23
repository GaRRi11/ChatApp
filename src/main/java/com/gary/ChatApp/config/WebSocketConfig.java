package com.gary.ChatApp.config;

import com.gary.ChatApp.interceptor.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
/*
* Sets up your WebSocket messaging infrastructure using STOMP protocol.

 Registers /chat as the WebSocket connection URL.

Adds your custom handshake interceptor to check JWT tokens during connection setup.

*/
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enables simple in-memory broker for topics and queues used in messaging.
        // Sets the prefix clients will use when sending messages to the server.
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers a WebSocket endpoint at /chat.
        // Adds the JWT handshake interceptor to authenticate users at handshake.
        // Enables SockJS fallback for browsers that don't support WebSocket.
        registry.addEndpoint("/chat")
                .addInterceptors(new JwtHandshakeInterceptor()) // optional for production
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

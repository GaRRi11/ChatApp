package com.gary.config;

import com.gary.ChatApp.infrastructure.websocket.JwtHandshakeInterceptor;
import com.gary.ChatApp.domain.service.presence.UserPresenceService;
import com.gary.ChatApp.infrastructure.websocket.PingMessageInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final UserPresenceService userPresenceService;
    private final PingMessageInterceptor pingMessageInterceptor; // add this

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("https://yourdomain.com") // restrict for production
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS();  // optional SockJS fallback

    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> new WebSocketHandlerConfig(handler, userPresenceService));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(pingMessageInterceptor); // register ping interceptor
    }
}

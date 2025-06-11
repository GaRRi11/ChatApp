package com.gary.config;

import com.gary.infrastructure.websocket.JwtHandshakeInterceptor;
import com.gary.domain.service.presence.UserPresenceService;
import com.gary.infrastructure.websocket.PingMessageInterceptor;
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
    private final PingMessageInterceptor pingMessageInterceptor;
    private static final String WEBSOCKET_ENDPOINT = "/ws";
    private static final String[] ALLOWED_ORIGINS = { "https://ChatApp.com" };

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WEBSOCKET_ENDPOINT)
                .setAllowedOrigins(ALLOWED_ORIGINS)
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS();

    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> new UserPresenceWebSocketHandlerDecorator(handler, userPresenceService));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(pingMessageInterceptor);
    }
}

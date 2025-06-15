package com.gary.infrastructure.websocket;

import com.gary.infrastructure.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenUtil jwtTokenUtil;
    private static final String USER_ID_ATTR = "user-id";
    private static final String TOKEN_PARAM = "token";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String uri = request.getURI().toString();
        String token = UriComponentsBuilder.fromUriString(uri).build().getQueryParams().getFirst(TOKEN_PARAM);

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null) {
            log.info("WebSocket handshake attempt from IP: {}", remoteAddress.getAddress());
        }


        if (token == null) {
            log.error("Missing token in handshake request URI: {}", uri);
            return false;
        }

        try {
            if (jwtTokenUtil.validateToken(token)) {
                UUID userId = jwtTokenUtil.extractUserId(token);
                if (userId == null) {
                    log.error("Extracted null userId from token in URI {}", uri);
                    return false;
                }
                attributes.put(USER_ID_ATTR, userId);
                return true;
            } else {
                log.error("Invalid JWT token in handshake for URI {}", uri);
                return false;
            }
        } catch (Exception ex) {
            log.error("Exception validating JWT token in handshake: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception != null) {
            log.error("Handshake failed with exception: {}", exception.getMessage(), exception);
        }
    }
}

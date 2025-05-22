package com.gary.ChatApp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;


/*

 Checks the JWT token before
 the WebSocket connection is established.

You can block unauthorized clients by returning false.

Allows you to pass user data into WebSocket sessions for later use.

 */
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        // Extract JWT token from headers or query params
        // Example:
        // String token = ((ServletServerHttpRequest) request).getServletRequest().getParameter("token");

        // Validate the token (use your JwtTokenProvider or similar)
        // If valid, you can add user info to attributes

        return true; // return false to reject the connection
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // You can log or clean up here if needed
    }
}

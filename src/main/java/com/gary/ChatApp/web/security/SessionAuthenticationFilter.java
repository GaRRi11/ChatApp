package com.gary.ChatApp.web.security;

import com.gary.ChatApp.storage.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.LogRecord;

@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter implements Filter {


    private final SessionManager sessionManager;

    private final UserRepository userRepository;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        if (requestURI.startsWith("/auth")) {
            // Skip authentication for the /auth endpoint
            chain.doFilter(request, response);
            return;
        }

        // Continue with session authentication for other endpoints
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            String sessionId = sessionManager.getSessionIdFromCookie(cookies);
            if (sessionId != null) {

            Long userId = sessionManager.getUserIdFromSession(sessionId);
            if (userId != null && UserContext.getUser() == null) {
                if (this.userRepository.findById(userId).isEmpty()){
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.getOutputStream().write("The provided session is invalid or has expired.".getBytes());
                    return;
                }
                UserContext.setUser(userRepository.findById(userId).orElse(null));
                // User is authenticated, proceed with the request
                chain.doFilter(request, response);
                return;
            }
        }
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}

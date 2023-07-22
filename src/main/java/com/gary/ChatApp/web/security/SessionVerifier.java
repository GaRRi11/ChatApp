package com.gary.ChatApp.web.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class SessionVerifier {
    private final SessionManager sessionManager;
    public boolean isSessionValid(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String sessionId = Arrays.stream(cookies)
                    .filter(cookie -> SessionManager.SESSION_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            if (sessionId != null) {

                Long userId = sessionManager.getUserIdFromSession(sessionId);

                if (userId != null) {
                    return true;
                }
            }
        }

        return false;
    }
}
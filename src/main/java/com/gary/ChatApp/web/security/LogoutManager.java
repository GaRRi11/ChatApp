package com.gary.ChatApp.web.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class LogoutManager {
    private final RedisTemplate template;
    public void logout(HttpServletRequest request,HttpServletResponse response) {
        Cookie sessionCookie = new Cookie(SessionManager.SESSION_COOKIE_NAME, null);
        sessionCookie.setMaxAge(0);
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String sessionId = Arrays.stream(cookies)
                    .filter(cookie -> SessionManager.SESSION_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            template.opsForValue().getAndDelete("session:" + sessionId);
        }
    }
}

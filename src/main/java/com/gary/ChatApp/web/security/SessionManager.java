package com.gary.ChatApp.web.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "sessionId";

    private final RedisTemplate template;


    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    public void createSession(Long userId,HttpServletResponse response){
        String sessionId = generateSessionId();
        String sessionKey = getSessionKey(sessionId);
        template.opsForValue().set(sessionKey,userId);
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        sessionCookie.setMaxAge(3600);
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);
    }

    public Long getUserIdFromSession(String sessionId) {
        String sessionKey = getSessionKey(sessionId);
        return (Long) template.opsForValue().get(sessionKey);
    }

    private String getSessionKey(String sessionId) {
        return "session:" + sessionId;
    }

}

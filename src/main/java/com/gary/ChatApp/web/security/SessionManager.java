package com.gary.ChatApp.web.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class SessionManager {

    private static final String SESSION_COOKIE_NAME = "sessionId";

    private final RedisTemplate template;


    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    public void createSession(String userId, HttpServletResponse response){
        String sessionId = generateSessionId();
        String sessionData = "{\"userId\":\"" + userId + "\",\"otherData\":\"value\"}"; //expiration davamato
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", userId);
//        sessionAttributes.put("username", session.getUsername());
//        sessionAttributes.put("otherData", session.getOtherData());

        template.opsForHash().putAll(sessionId, sessionAttributes);
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        sessionCookie.setMaxAge(1);
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);}

}

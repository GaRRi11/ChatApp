//package com.gary.ChatApp.web.security;
//
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//public class SessionManager {
//
//    public static final String SESSION_COOKIE_NAME = "sessionId";
//
//    private final RedisTemplate template;
//
//
//    private String generateSessionId() {
//        return UUID.randomUUID().toString();
//    }
//
//    public void createSession(Long userId,HttpServletResponse response){
//        String sessionId = generateSessionId();
//        String sessionKey = getSessionKey(sessionId);
//        template.opsForValue().set(sessionKey,userId);
//        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
//        sessionCookie.setMaxAge(3600);
//        sessionCookie.setHttpOnly(true);
//        response.addCookie(sessionCookie);
//    }
//
//    public String getSessionIdFromCookie(Cookie[] cookies){
//            String sessionId = Arrays.stream(cookies)
//                    .filter(cookie -> SessionManager.SESSION_COOKIE_NAME.equals(cookie.getName()))
//                    .map(Cookie::getValue)
//                    .findFirst()
//                    .orElse(null);
//        return sessionId;
//    }
//
//    public void logout(HttpServletRequest request,HttpServletResponse response){
//        Cookie sessionCookie = new Cookie(SessionManager.SESSION_COOKIE_NAME, null);
//        sessionCookie.setMaxAge(0);
//        sessionCookie.setHttpOnly(true);
//        response.addCookie(sessionCookie);
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            String sessionId = getSessionIdFromCookie(cookies);
//            template.opsForValue().getAndDelete("session:" + sessionId);
//        }
//    }
//
//    public Long getUserIdFromSession(String sessionId) {
//        String sessionKey = getSessionKey(sessionId);
//        return (Long) template.opsForValue().get(sessionKey);
//    }
//
//    private String getSessionKey(String sessionId) {
//        return "session:" + sessionId;
//    }
//
//}

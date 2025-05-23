package com.gary.ChatApp.exceptions;

import com.gary.ChatApp.domain.service.userPresenceService.UserPresenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
//UPDATES ONLINE STATUS
public class PresenceInterceptor implements HandlerInterceptor {



    private final UserPresenceService userPresenceService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdHeader = request.getHeader("user-id");

        if (userIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                userPresenceService.refreshOnlineStatus(userId);
            } catch (NumberFormatException ignored) {
                // Log if needed
            }
        }

        return true; // continue with the request
    }
}

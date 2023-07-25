package com.gary.ChatApp.web.security;

import com.gary.ChatApp.storage.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            String sessionId = sessionManager.getSessionIdFromCookie(cookies);
            if (sessionId != null) {
            Long userId = sessionManager.getUserIdFromSession(sessionId);
            if (userId != null) {
                if (this.userRepository.findById(userId).isEmpty()){
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.getOutputStream().write("The provided session is invalid or has expired.".getBytes());
                    return;
                }
                if (UserContext.getUser() == null){
                    UserContext.setUser(userRepository.findById(userId).orElse(null));
                }
                chain.doFilter(request, response);
                return;
            }

        }
        }
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getOutputStream().write("The provided session is invalid or has expired.".getBytes());
    }


    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}

package com.gary.admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class IpWhitelistFilter extends OncePerRequestFilter {

    private final List<String> whitelist = List.of("127.0.0.1", "192.168.1.100");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        if (!whitelist.contains(ip)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden IP");
            return;
        }

        filterChain.doFilter(request, response);
    }
}


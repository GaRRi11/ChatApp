package com.gary.infrastructure.security;

import com.gary.domain.model.user.User;
import com.gary.domain.service.user.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    public JwtTokenFilter(JwtTokenUtil jwtTokenUtil, UserService userService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Optional<String> tokenOpt = extractToken(request);

            if (tokenOpt.isEmpty()) {
                // No token provided, continue without authentication (let Spring Security handle it)
                log.debug("No JWT token found in request headers");
                filterChain.doFilter(request, response);
                return;
            }

            String token = tokenOpt.get();

            if (!jwtTokenUtil.validateToken(token)) {
                log.warn("Invalid or expired JWT token");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            Long userId = jwtTokenUtil.extractUserId(token);

            Optional<User> userOpt = userService.getById(userId);
            if (userOpt.isEmpty()) {
                log.warn("User not found with ID extracted from JWT token");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }

            User user = userOpt.get();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            null
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authentication successful for user ID: {}", userId);

        } catch (JwtException e) {
            log.warn("JWT processing failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or malformed token");
            return;
        }

        filterChain.doFilter(request, response);
    }


    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(header.substring(BEARER_PREFIX.length()));
    }
}

package com.gary.infrastructure.security;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



import java.security.Key;
import java.util.Date;
import io.jsonwebtoken.security.Keys;

@Component
@Slf4j
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final long REFRESH_TOKEN_EXPIRATION_MS = ACCESS_TOKEN_EXPIRATION_MS * 7; // 7 days

    private Key signingKey;

    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtSecret);
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Failed to initialize JWT signing key", e);
            throw new IllegalArgumentException("Invalid jwt.secret", e);
        }
    }

    public String generateAccessToken(Long userId, String username) {
        return buildToken(userId, username, ACCESS_TOKEN_EXPIRATION_MS);
    }

    public String generateRefreshToken(Long userId, String username) {
        return buildToken(userId, username, REFRESH_TOKEN_EXPIRATION_MS);
    }

    private String buildToken(Long userId, String username, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public Long extractUserId(String token) {
        try {
            return Long.valueOf(extractAllClaims(token).get("id").toString());
        } catch (Exception e) {
            log.error("Failed to extract user ID from token", e);
            throw new JwtException("Invalid token");
        }
    }

    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (Exception e) {
            log.error("Failed to extract username from token", e);
            throw new JwtException("Invalid token");
        }
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT token malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is null or empty: {}", e.getMessage());
        }
        return false;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

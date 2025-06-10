package com.gary.domain.service.refreshToken;

import com.gary.config.RedisKeys;
import com.gary.domain.model.user.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RedisTemplate<String, RefreshToken> refreshTokenRedisTemplate;

    private static final long REFRESH_TOKEN_TTL = 7 * 24 * 60 * 60; // 7 days in seconds

    @Override
    public void save(Long userId, String token) {
        String key = RedisKeys.refreshToken(token);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_TTL).toEpochMilli())
                .revoked(false)
                .build();

        refreshTokenRedisTemplate.opsForValue().set(key, refreshToken);
        refreshTokenRedisTemplate.expire(key, Duration.ofSeconds(REFRESH_TOKEN_TTL));

        log.debug("Stored refresh token in Redis: {}", key);

        // Optionally track token per user for global logout support
        String userTokenSetKey = RedisKeys.refreshTokenSet(userId);
        refreshTokenRedisTemplate.opsForSet().add(userTokenSetKey, refreshToken);
        refreshTokenRedisTemplate.expire(userTokenSetKey, Duration.ofSeconds(REFRESH_TOKEN_TTL));
    }

    @Override
    public boolean isValid(String token) {
        String key = RedisKeys.refreshToken(token);
        RefreshToken refreshToken = refreshTokenRedisTemplate.opsForValue().get(key);

        if (refreshToken == null) {
            log.warn("Refresh token not found or expired: {}", token);
            return false;
        }

        if (refreshToken.getExpiryDate() < Instant.now().toEpochMilli()) {
            log.info("Refresh token expired: {}", token);
            refreshTokenRedisTemplate.delete(key);
            return false;
        }

        if (refreshToken.isRevoked()) {
            log.info("Refresh token has been revoked: {}", token);
            return false;
        }

        return true;
    }

    @Override
    public void revoke(String token) {
        String key = RedisKeys.refreshToken(token);
        RefreshToken refreshToken = refreshTokenRedisTemplate.opsForValue().get(key);

        if (refreshToken != null) {
            refreshToken.setRevoked(true);
            refreshTokenRedisTemplate.opsForValue().set(key, refreshToken);
            refreshTokenRedisTemplate.expire(key, Duration.ofSeconds(REFRESH_TOKEN_TTL));
            log.info("Refresh token marked as revoked: {}", token);
        } else {
            log.warn("Attempted to revoke non-existent refresh token: {}", token);
        }
    }

    @Override
    public void revokeAll(Long userId) {
        String userTokenSetKey = RedisKeys.refreshTokenSet(userId);
        var tokens = refreshTokenRedisTemplate.opsForSet().members(userTokenSetKey);

        if (tokens != null) {
            for (Object obj : tokens) {
                if (obj instanceof RefreshToken refreshToken) {
                    refreshToken.setRevoked(true);
                    String tokenKey = RedisKeys.refreshToken(refreshToken.getToken());
                    refreshTokenRedisTemplate.opsForValue().set(tokenKey, refreshToken);
                    refreshTokenRedisTemplate.expire(tokenKey, Duration.ofSeconds(REFRESH_TOKEN_TTL));
                }
            }
        }

        log.info("Revoked all refresh tokens for user: {}", userId);
    }
}

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
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RedisTemplate<String, RefreshToken> refreshTokenRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;

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

        refreshTokenRedisTemplate.opsForValue()
                .set(key, refreshToken, Duration.ofSeconds(REFRESH_TOKEN_TTL));

        log.debug("Stored refresh token in Redis: {}", key);

        // Store token string (not whole object) to user set
        String userTokenSetKey = RedisKeys.refreshTokenSet(userId);
        stringRedisTemplate.opsForSet().add(userTokenSetKey, token);
        stringRedisTemplate.expire(userTokenSetKey, Duration.ofSeconds(REFRESH_TOKEN_TTL));
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

            Long ttlSeconds = refreshTokenRedisTemplate.getExpire(key, java.util.concurrent.TimeUnit.SECONDS);
            Duration ttl = ttlSeconds != null && ttlSeconds > 0 ? Duration.ofSeconds(ttlSeconds) : Duration.ofSeconds(REFRESH_TOKEN_TTL);
            refreshTokenRedisTemplate.opsForValue().set(key, refreshToken, ttl);

            log.info("Refresh token marked as revoked: {}", token);
        } else {
            log.warn("Attempted to revoke non-existent refresh token: {}", token);
        }
    }

    @Override
    public void revokeAll(Long userId) {
        String userTokenSetKey = RedisKeys.refreshTokenSet(userId);
        var tokens = stringRedisTemplate.opsForSet().members(userTokenSetKey);

        if (tokens != null) {
            for (String token : tokens) {
                String tokenKey = RedisKeys.refreshToken(token);
                RefreshToken refreshToken = refreshTokenRedisTemplate.opsForValue().get(tokenKey);

                if (refreshToken != null) {
                    refreshToken.setRevoked(true);
                    Long ttlSeconds = refreshTokenRedisTemplate.getExpire(tokenKey, java.util.concurrent.TimeUnit.SECONDS);
                    Duration ttl = ttlSeconds != null && ttlSeconds > 0 ? Duration.ofSeconds(ttlSeconds) : Duration.ofSeconds(REFRESH_TOKEN_TTL);
                    refreshTokenRedisTemplate.opsForValue().set(tokenKey, refreshToken, ttl);
                }
            }
        }

        log.info("Revoked all refresh tokens for user: {}", userId);
    }

}

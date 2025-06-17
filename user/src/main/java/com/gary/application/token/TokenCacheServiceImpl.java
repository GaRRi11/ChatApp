package com.gary.application.token;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.common.ResultStatus;
import com.gary.domain.model.token.RefreshToken;
import com.gary.domain.service.token.TokenCacheService;
import com.gary.infrastructure.constants.RedisKeys;
import com.gary.application.token.TokenResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCacheServiceImpl implements TokenCacheService {

    private final RedisTemplate<String, RefreshToken> refreshTokenRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final MeterRegistry meterRegistry;

    private static final Duration TOKEN_EXPIRATION = Duration.ofDays(7);

    @Override
    @LoggableAction("Cache Refresh Token")
    @Timed("token.cache.cacheToken.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "cacheTokenFallback")
    public void cache(RefreshToken token) {
        String tokenKey = RedisKeys.refreshToken(token.getToken());
        refreshTokenRedisTemplate.opsForValue().set(tokenKey, token, TOKEN_EXPIRATION);

        String userTokenSetKey = RedisKeys.refreshTokenSet(token.getUserId());
        stringRedisTemplate.opsForSet().add(userTokenSetKey, token.getToken());
        stringRedisTemplate.expire(userTokenSetKey, TOKEN_EXPIRATION);

        log.info("Cached refresh token for user {} with key {}", token.getUserId(), tokenKey);
        meterRegistry.counter("token.cache", "status", "success").increment();
    }

    @Override
    @LoggableAction("Cache Refresh Token Fallback")
    public void cacheTokenFallback(RefreshToken token, Throwable t) {
        log.warn("Failed to cache refresh token for user {}: {}", token.getUserId(), t.getMessage());
        meterRegistry.counter("token.cache", "status", "fallback").increment();
    }

    @Override
    @LoggableAction("Get Cached Refresh Token")
    @Timed("token.cache.getToken.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getTokenFallback")
    public TokenResult get(String token) {

        RefreshToken refreshToken = refreshTokenRedisTemplate.opsForValue().get(RedisKeys.refreshToken(token));

        if (refreshToken != null) {
            log.info("Retrieved cached refresh token for token {}", token);
            meterRegistry.counter("token.cache", "status", "hit").increment();
            return new TokenResult(refreshToken, ResultStatus.HIT);
        } else {
            log.info("No cached refresh token found for token {}", token);
            meterRegistry.counter("token.cache", "status", "miss").increment();
            return new TokenResult(null, ResultStatus.MISS);
        }
    }

    @Override
    @LoggableAction("Get Cached Refresh Token Fallback")
    public TokenResult getTokenFallback(String token, Throwable t) {
        log.warn("Failed to get refresh token for token {}: {}", token, t.getMessage());
        meterRegistry.counter("token.cache", "status", "fallback_get").increment();
        return new TokenResult(null, ResultStatus.FALLBACK);
    }

    @Override
    @LoggableAction("Delete Cached Refresh Token")
    @Timed("token.cache.deleteToken.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "deleteTokenFallback")
    public void delete(String token) {
        String tokenKey = RedisKeys.refreshToken(token);
        RefreshToken tokenObj = refreshTokenRedisTemplate.opsForValue().get(tokenKey);

        if (tokenObj != null) {
            tokenObj.setRevoked(true);
            Long ttl = refreshTokenRedisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
            Duration duration = ttl != null && ttl > 0 ? Duration.ofSeconds(ttl) : TOKEN_EXPIRATION;
            refreshTokenRedisTemplate.opsForValue().set(tokenKey, tokenObj, duration);
        }
    }

    @Override
    @LoggableAction("Delete Cached Refresh Token Fallback")
    public void deleteTokenFallback(String token, Throwable t) {
        log.warn("Failed to delete refresh token for token {}: {}", token, t.getMessage());
        meterRegistry.counter("token.cache", "status", "fallback_delete").increment();
    }

    @Override
    @LoggableAction("Revoke All Cached Tokens")
    @Timed("token.cache.revokeAll.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "revokeAllTokensFallback")
    public void revokeAll(UUID userId) {
        String userTokenSetKey = RedisKeys.refreshTokenSet(userId);
        var tokens = stringRedisTemplate.opsForSet().members(userTokenSetKey);

        if (tokens != null) {
            for (String token : tokens) {
                String tokenKey = RedisKeys.refreshToken(token);
                RefreshToken refreshToken = refreshTokenRedisTemplate.opsForValue().get(tokenKey);

                if (refreshToken != null) {
                    refreshToken.setRevoked(true);
                    Long ttlSeconds = refreshTokenRedisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
                    Duration ttl = ttlSeconds != null && ttlSeconds > 0
                            ? Duration.ofSeconds(ttlSeconds)
                            : TOKEN_EXPIRATION;
                    refreshTokenRedisTemplate.opsForValue().set(tokenKey, refreshToken, ttl);
                }
            }
        }

        log.info("Revoked all refresh tokens for user: {}", userId);
        meterRegistry.counter("token.cache", "status", "revoke_all").increment();
    }

    @LoggableAction("Revoke All Cached Tokens Fallback")
    public void revokeAllTokensFallback(UUID userId, Throwable t) {
        log.warn("Failed to revoke all refresh tokens for user {}: {}", userId, t.getMessage());
        meterRegistry.counter("token.cache", "status", "fallback_revoke_all").increment();
    }

}

package com.gary.application.token;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.common.ResultStatus;
import com.gary.domain.model.token.RefreshToken;
import com.gary.domain.repository.token.RefreshTokenRepository;
import com.gary.domain.service.token.TokenPersistenceService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenPersistenceServiceImpl implements TokenPersistenceService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MeterRegistry meterRegistry;

    @Override
    @LoggableAction("Save Refresh Token")
    @Timed("token.db.saveToken.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "saveTokenFallback")
    public RefreshToken save(RefreshToken token) {
        RefreshToken saved = refreshTokenRepository.save(token);
        log.info("Saved refresh token with id {} for user {}", saved.getId(), saved.getUserId());
        meterRegistry.counter("token.save", "status", "success").increment();
        return saved;
    }

    @Override
    @LoggableAction("Save Refresh Token Fallback")
    public RefreshToken saveTokenFallback(RefreshToken token, Throwable t) {
        log.error("Failed to save refresh token for user {}: {}", token.getUserId(), t.getMessage());
        meterRegistry.counter("token.save", "status", "fallback").increment();
        return null;
    }

    @Override
    @LoggableAction("Get Refresh Token")
    @Timed("token.db.getToken.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getTokenFallback")
    public TokenResult get(UUID userId) {
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByUserId(userId);

        if (optionalToken.isPresent()) {
            RefreshToken token = optionalToken.get();
            log.info("Retrieved refresh token for user {}", userId);
            meterRegistry.counter("token.get", "status", "success").increment();
            return new TokenResult(token, ResultStatus.HIT);
        } else {
            log.info("No refresh token found in DB for user {}", userId);
            meterRegistry.counter("token.get", "status", "miss").increment();
            return new TokenResult(null, ResultStatus.MISS);
        }
    }

    @Override
    @LoggableAction("Get Refresh Token Fallback")
    public TokenResult getTokenFallback(UUID userId, Throwable t) {
        log.warn("Failed to retrieve refresh token for user {}: {}", userId, t.getMessage());
        meterRegistry.counter("token.get", "status", "fallback").increment();
        return new TokenResult(null, ResultStatus.FALLBACK);
    }

    @Override
    @LoggableAction("Delete Refresh Token")
    @Timed("token.db.deleteToken.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "deleteTokenFallback")
    public void delete(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("Deleted refresh token for user {}", userId);
    }

    @Override
    @LoggableAction("Delete Refresh Token Fallback")
    public void deleteTokenFallback(UUID userId, Throwable t) {
        log.warn("Failed to delete refresh token for user {}: {}", userId, t.getMessage());
        meterRegistry.counter("token.delete", "status", "fallback").increment();
    }
}

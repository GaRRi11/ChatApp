package com.gary.application.token;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.common.ResultStatus;
import com.gary.domain.model.token.RefreshToken;
import com.gary.domain.repository.token.RefreshTokenRepository;
import com.gary.infrastructure.security.JwtTokenUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MeterRegistry meterRegistry;

    private final long REFRESH_TOKEN_DURATION_MS = 7 * 24 * 60 * 60 * 1000; // 7 days


    @LoggableAction("Create Refresh Token")
    @Timed("refreshToken.create.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "createFallback")
    public RefreshToken create(UUID userId) {
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .revoked(false)
                .expiryDate(Instant.now().toEpochMilli() + REFRESH_TOKEN_DURATION_MS)
                .build();
        meterRegistry.counter("refresh.token.create", "status", "success").increment();
        return refreshTokenRepository.save(token);
    }

    public RefreshToken createFallback(UUID userId, Throwable t) {
        log.warn("Failed to create refresh token for {} due to {}", userId, t.getMessage());
        meterRegistry.counter("refresh.token.create", "status", "fallback").increment();
        return null;
    }


    @LoggableAction("Verify Refresh Token")
    @Timed("refreshToken.verify.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "verifyFallback")
    public RefreshTokenResponse verify(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate() < Instant.now().toEpochMilli()) {
            return RefreshTokenResponse.builder()
                    .refreshToken(null)
                    .resultStatus(ResultStatus.MISS)
                    .build();
        }

        meterRegistry.counter("refresh.token.verify", "status", "success").increment();

        return RefreshTokenResponse.builder()
                .refreshToken(refreshToken)
                .resultStatus(ResultStatus.HIT)
                .build();
    }

    public RefreshTokenResponse verifyFallback(String token, Throwable t) {
        log.warn("Failed to verify refresh token for {} to {}", token, t);
        meterRegistry.counter("refresh.token.verify", "status", "fallback").increment();
        return RefreshTokenResponse.builder()
                .refreshToken(null)
                .resultStatus(ResultStatus.FALLBACK)
                .build();
    }


    @Transactional
    @LoggableAction("Delete Refresh Tokens")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "deleteByUserFallback")
    public boolean deleteByUser(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
        meterRegistry.counter("refresh.token.delete", "status", "success").increment();
        return true;
    }

    public boolean deleteByUserFallback(UUID userId, Throwable t) {
        log.warn("Failed to delete refresh token for {} to {}", userId, t);
        meterRegistry.counter("refresh.token.delete", "status", "fallback").increment();
        return false;
    }


        @LoggableAction("Clear Expired Refresh Tokens")
    @Timed("refreshToken.clearExpired.duration")
    public void clearExpiredTokens() {
        long now = Instant.now().toEpochMilli();
        try {
            refreshTokenRepository.findAll().stream()
                    .filter(token -> token.getExpiryDate() < now)
                    .forEach(token -> refreshTokenRepository.deleteById(token.getId()));
            meterRegistry.counter("refresh.token.delete.expired", "status", "success").increment();
        } catch (Exception e) {
            log.warn("Failed to delete expired tokens: {}", e.getMessage());
            meterRegistry.counter("refresh.token.delete.expired", "status", "failed").increment();
        }
    }
}

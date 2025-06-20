package com.gary.application.token;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.common.MetricIncrement;
import com.gary.application.common.ResultStatus;
import com.gary.application.common.TimeFormat;
import com.gary.domain.model.token.RefreshToken;
import com.gary.domain.repository.token.RefreshTokenRepository;
import com.gary.domain.service.refreshToken.RefreshTokenService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MetricIncrement metricIncrement;

    @Value("${auth.refresh-token.duration-ms}")
    private long refreshTokenDurationMs;

    @Override
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
                .expiryDate(Instant.now().toEpochMilli() + refreshTokenDurationMs)
                .build();

        metricIncrement.incrementMetric("refresh.token.save","success");
        return refreshTokenRepository.save(token);
    }

    RefreshToken createFallback(UUID userId, Throwable t) {
        log.warn("Timestamp='{}' Failed to create refresh token for userId={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                t.toString());
        metricIncrement.incrementMetric("refresh.token.save","fallback");
        return null;
    }


    @Override
    @LoggableAction("Verify Refresh Token")
    @Timed("refreshToken.verify.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "verifyFallback")
    public RefreshTokenResponse verify(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate() < Instant.now().toEpochMilli()) {
            log.warn("Timestamp='{}' Refresh token '{}' is revoked or expired. Returning MISS status.",
                    TimeFormat.nowTimestamp(),
                    token);

            return RefreshTokenResponse.builder()
                    .refreshToken(null)
                    .resultStatus(ResultStatus.MISS)
                    .build();
        }

        metricIncrement.incrementMetric("refresh.token.verify","success");

        return RefreshTokenResponse.builder()
                .refreshToken(refreshToken)
                .resultStatus(ResultStatus.HIT)
                .build();
    }

    RefreshTokenResponse verifyFallback(String token, Throwable t) {
        log.warn("Timestamp='{}' Failed to verify refresh token '{}'. Cause: {}",
                TimeFormat.nowTimestamp(),
                token,
                t.toString());

        metricIncrement.incrementMetric("refresh.token.verify","fallback");

        return RefreshTokenResponse.builder()
                .refreshToken(null)
                .resultStatus(ResultStatus.FALLBACK)
                .build();
    }


    @Override
    @Transactional
    @LoggableAction("Delete Refresh Tokens")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "deleteByUserFallback")
    public boolean deleteByUser(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
        metricIncrement.incrementMetric("refresh.token.delete","success");
        return true;
    }

    boolean deleteByUserFallback(UUID userId, Throwable t) {
        log.warn("Timestamp='{}' Failed to delete refresh token for userId={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                t.toString());
        metricIncrement.incrementMetric("refresh.token.delete","fallback");
        return false;
    }


    @Override
    @LoggableAction("Clear Expired Refresh Tokens")
    @Timed("refreshToken.clearExpired.duration")
    public void clearExpiredTokens() {
        long now = Instant.now().toEpochMilli();
        try {
            refreshTokenRepository.deleteExpiredTokens(Instant.now().toEpochMilli());
            metricIncrement.incrementMetric("refresh.token.delete.expired","success");
        } catch (RuntimeException e) {
            log.warn("Timestamp='{}' Failed to delete expired tokens. Cause: {}",
                    TimeFormat.nowTimestamp(),
                    e.toString());
            metricIncrement.incrementMetric("refresh.token.delete.expired","fallback");
        }
    }
}

package com.gary.application.token;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.domain.model.token.RefreshToken;
import com.gary.domain.repository.jpa.token.RefreshTokenRepository;
import com.gary.domain.service.refreshToken.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${auth.refresh-token.duration-ms}")
    private long refreshTokenDurationMs;


    public Optional<RefreshToken> getTokenObject(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.SERIALIZABLE
    )
    @LoggableAction("Create Refresh Token")
    @Timed("refreshToken.create.duration")
    public RefreshToken create(UUID userId) {

        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .revoked(false)
                .expiryDate(Instant.now().toEpochMilli() + refreshTokenDurationMs)
                .build();

        try {
            return refreshTokenRepository.save(token);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent refresh token creation detected, retrying fetch...");
            return refreshTokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Token creation race condition"));
        }
    }


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteByUser(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Timed("refreshToken.clearExpired.duration")
    public void clearExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now().toEpochMilli());
    }

    @Override
    public Optional<RefreshToken> findByUserId(UUID userId) {
        return refreshTokenRepository.findByUserId(userId);
    }
}

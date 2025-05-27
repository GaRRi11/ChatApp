package com.gary.ChatApp.domain.service.user;

import com.gary.ChatApp.domain.model.user.RefreshToken;
import com.gary.ChatApp.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void save(Long userId, String token) {
        log.debug("Storing refresh token for userId={}", userId);

        refreshTokenRepository.deleteByUserId(userId); // invalidate previous

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiryDate(Instant.now().plusSeconds(7 * 24 * 3600).toEpochMilli())
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        log.debug("Refresh token stored for userId={}", userId);
    }

    @Override
    public boolean isValid(String token) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) return false;

        RefreshToken refreshToken = tokenOpt.get();

        boolean valid = !refreshToken.isRevoked() &&
                refreshToken.getExpiryDate() > Instant.now().toEpochMilli();

        if (!valid) {
            log.warn("Invalid refresh token: token={}, revoked={}, expired={}",
                    token, refreshToken.isRevoked(),
                    refreshToken.getExpiryDate() < Instant.now().toEpochMilli());
        }

        return valid;
    }

    @Override
    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("Refresh token revoked for userId={}", rt.getUserId());
        });
    }
}

package com.gary.domain.service.refreshToken;

import com.gary.domain.model.token.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken create(UUID userId);

    void deleteByUser(UUID userId);

    void clearExpiredTokens();

    Optional<RefreshToken> findByUserId(UUID userId);

    Optional<RefreshToken> getTokenObject(String token);

    RefreshToken verifyExpiration(RefreshToken token);
}

package com.gary.domain.service.refreshToken;

import com.gary.application.token.RefreshTokenResponse;
import com.gary.domain.model.token.RefreshToken;

import java.util.UUID;

public interface RefreshTokenService {
     RefreshToken create(UUID userId);
     RefreshTokenResponse verify(String token);
     boolean deleteByUser(UUID userId);
     void clearExpiredTokens();
    }

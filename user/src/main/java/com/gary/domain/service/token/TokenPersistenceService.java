package com.gary.domain.service.token;

import com.gary.application.token.TokenResult;
import com.gary.domain.model.token.RefreshToken;

import java.util.UUID;

public interface TokenPersistenceService {
     RefreshToken save(RefreshToken token);
     RefreshToken saveTokenFallback(RefreshToken token, Throwable t);
     TokenResult get(UUID userId);
     TokenResult getTokenFallback(UUID userId, Throwable t);
     void delete(UUID userId);
     void deleteTokenFallback(UUID userId, Throwable t);
    }

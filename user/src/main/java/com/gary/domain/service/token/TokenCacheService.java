package com.gary.domain.service.token;

import com.gary.application.token.TokenResult;
import com.gary.domain.model.token.RefreshToken;

import java.util.UUID;

public interface TokenCacheService {
     void cache(RefreshToken token);
     void cacheTokenFallback(RefreshToken token, Throwable t);
     TokenResult get(String token);
     TokenResult getTokenFallback(String token, Throwable t);
     void delete(String token);
     void deleteTokenFallback(String token, Throwable t);
     void revokeAll(UUID userId);
      void revokeAllTokensFallback(UUID userId, Throwable t);

     }

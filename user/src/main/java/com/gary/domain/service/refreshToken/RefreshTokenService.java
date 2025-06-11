package com.gary.domain.service.refreshToken;

public interface RefreshTokenService {
     void save(Long userId, String token);

     boolean isValid(String token);

     void revoke(String token);

     void revokeAll(Long userId);

    }

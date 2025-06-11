package com.gary.domain.service.token;

public interface RefreshTokenService {
     void save(Long userId, String token);

     boolean isValid(String token);

     void revoke(String token);

     void revokeAll(Long userId);

    }

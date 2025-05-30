package com.gary.domain.service.user;

public interface RefreshTokenService {
     void save(Long userId, String token);

     boolean isValid(String token);

     void revoke(String token);
    }

package com.gary.domain.service.token;

import java.util.UUID;

public interface RefreshTokenService {
     void save(UUID userId, String token);

     boolean isValid(String token);

     void revoke(String token);

     void revokeAll(UUID userId);

    }

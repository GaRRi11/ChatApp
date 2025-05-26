package com.gary.ChatApp.domain.service.user;

import com.gary.ChatApp.domain.model.user.RefreshToken;
import com.gary.ChatApp.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void save(Long userId, String token) {
        // Optional: delete old tokens for user or keep multiple
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 3600).toEpochMilli()); // e.g. 7 days
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
    }

    public boolean isValid(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if (refreshTokenOpt.isEmpty()) return false;

        RefreshToken refreshToken = refreshTokenOpt.get();
        if (refreshToken.isRevoked()) return false;
        if (refreshToken.getExpiryDate() < Instant.now().toEpochMilli()) return false;

        return true;
    }

    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}

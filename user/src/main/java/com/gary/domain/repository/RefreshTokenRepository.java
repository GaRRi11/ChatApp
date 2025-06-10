package com.gary.domain.repository;

import com.gary.domain.model.user.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Transactional
    void deleteByUserId(Long userId);

    List<RefreshToken> findAllByUserId(Long userId);
}

package com.gary.application.user;

import com.gary.domain.model.user.User;
import com.gary.domain.repository.user.UserRepository;
import com.gary.domain.service.token.RefreshTokenService;
import com.gary.domain.service.user.UserService;
import com.gary.web.exception.DuplicateResourceException;
import com.gary.web.exception.UnauthorizedException;
import com.gary.infrastructure.security.JwtTokenUtil;
import com.gary.web.dto.loginResponse.LoginResponseDto;
import com.gary.web.dto.user.UserRequest;
import com.gary.web.dto.user.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;
    private final MeterRegistry meterRegistry;


    @Override
    @Transactional
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "fallbackRegister")
    public UserResponse register(UserRequest userRequest) {
        log.info("Registering new user: username={}", userRequest.username());

        userRepository.findByUsername(userRequest.username()).ifPresent(u -> {
            log.warn("Registration failed: username '{}' already exists", userRequest.username());
            throw new DuplicateResourceException("Username already exists");
        });

        User user = userRepository.save(User.builder()
                .username(userRequest.username())
                .password(passwordEncoder.encode(userRequest.password()))
                .createdAt(LocalDateTime.now())
                .build());

        log.info("User registered successfully: userId={}, username={}", user.getId(), user.getUsername());

        meterRegistry.counter("user.register", "status", "success").increment();
        return UserResponse.fromEntity(user);
    }

    public UserResponse fallbackRegister(UserRequest userRequest, Throwable e) {
        meterRegistry.counter("user.register", "status", "failure").increment();
        log.error("Register fallback triggered: {}", e.getMessage());
        throw new RuntimeException("Registration service temporarily unavailable. Try again later.");
    }

    @Override
    public List<UserResponse> searchByUsername(String username, UUID requesterId) {
        log.info("Searching users by username='{}', requested by userId={}", username, requesterId);
        try {
            List<UserResponse> result = userRepository.findByUsernameContainingIgnoreCase(username).stream()
                    .filter(user -> !user.getId().equals(requesterId))
                    .map(UserResponse::fromEntity)
                    .toList();

            meterRegistry.counter("user.search", "status", result.isEmpty() ? "empty" : "success").increment();
            return result;
        } catch (Exception e) {
            meterRegistry.counter("user.search", "status", "failure").increment();
            log.error("Search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "fallbackLogin")
    public LoginResponseDto login(UserRequest userRequest) {
        log.info("Login attempt: username={}", userRequest.username());

        User user = userRepository.findByUsername(userRequest.username())
                .orElseThrow(() -> {
                    log.warn("Login failed: username '{}' not found", userRequest.username());
                    meterRegistry.counter("user.login", "status", "not_found").increment();
                    return new UnauthorizedException("Invalid credentials");
                });

        if (!passwordEncoder.matches(userRequest.password(), user.getPassword())) {
            meterRegistry.counter("user.login", "status", "invalid_password").increment();
            log.warn("Login failed: invalid password for username '{}'", userRequest.username());
            throw new UnauthorizedException("Invalid credentials");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());

        try {
            refreshTokenService.save(user.getId(), refreshToken);
        } catch (Exception e) {
            meterRegistry.counter("user.refresh_token", "status", "cache_fail").increment();
            log.warn("Failed to store refresh token: {}", e.getMessage());
        }
        log.info("Login successful: userId={}, username={}", user.getId(), user.getUsername());

        meterRegistry.counter("user.login", "status", "success").increment();

        return LoginResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public LoginResponseDto fallbackLogin(UserRequest userRequest, Throwable e) {
        meterRegistry.counter("user.login", "status", "failure").increment();
        log.error("Login fallback triggered: {}", e.getMessage());
        throw new RuntimeException("Login service temporarily unavailable. Try again later.");
    }

    @Override
    public void logout(User user) {
        log.info("Logout requested: userId={}, username={}", user.getId(), user.getUsername());
        try {
            refreshTokenService.revokeAll(user.getId());
            meterRegistry.counter("user.logout", "status", "success").increment();
            log.info("Logout successful: userId={}, username={}", user.getId(), user.getUsername());
        } catch (Exception e) {
            meterRegistry.counter("user.logout", "status", "failure").increment();
            log.warn("Failed to revoke tokens during logout: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "fallbackRefreshToken")
    public LoginResponseDto refreshToken(String token) {
        if (!jwtTokenUtil.validateToken(token)) {
            meterRegistry.counter("user.refresh_token", "status", "invalid_jwt").increment();
            log.warn("Refresh token validation failed: reason=invalid JWT");
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (!refreshTokenService.isValid(token)) {
            meterRegistry.counter("user.refresh_token", "status", "revoked").increment();
            log.warn("Refresh token validation failed: reason=revoked or expired");
            throw new UnauthorizedException("Invalid refresh token");
        }

        UUID userId = jwtTokenUtil.extractUserId(token);
        String username = jwtTokenUtil.extractUsername(token);

        log.info("Refreshing access token for userId={}, username={}", userId, username);

        refreshTokenService.revoke(token); // revoke old token
        String newAccessToken = jwtTokenUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, username);

        try {
            refreshTokenService.save(userId, newRefreshToken);
        } catch (Exception e) {
            meterRegistry.counter("user.refresh_token", "status", "cache_fail").increment();
            log.warn("Failed to store new refresh token: {}", e.getMessage());
        }

        log.info("Refresh token succeeded: userId={}, username={}", userId, username);

        return LoginResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public LoginResponseDto fallbackRefreshToken(String token, Throwable e) {
        meterRegistry.counter("user.refresh_token", "status", "failure").increment();
        log.error("Refresh fallback triggered: {}", e.getMessage());
        throw new RuntimeException("Token refresh service temporarily unavailable.");
    }

    @Override
    public Optional<User> getById(UUID id) {
        meterRegistry.counter("user.get_by_id", "status", "hit").increment();
        log.debug("Fetching user by ID: userId={}", id);
        try {
            return userRepository.findById(id);
        } catch (Exception e) {
            meterRegistry.counter("user.get_by_id", "status", "failure").increment();
            log.warn("getById failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAllById(List<UUID> userIds) {
        log.debug("Fetching multiple users by IDs: count={}", userIds.size());
        try {
            meterRegistry.counter("user.find_all_by_id", "status", "success").increment();
            return userRepository.findAllById(userIds);
        } catch (Exception e) {
            meterRegistry.counter("user.find_all_by_id", "status", "failure").increment();
            log.warn("findAllById failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

}

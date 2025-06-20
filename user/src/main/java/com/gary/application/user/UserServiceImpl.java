package com.gary.application.user;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.common.MetricIncrement;
import com.gary.application.common.ResultStatus;
import com.gary.application.common.TimeFormat;
import com.gary.application.token.RefreshTokenResponse;
import com.gary.application.token.RefreshTokenServiceImpl;
import com.gary.domain.model.user.User;
import com.gary.domain.repository.user.UserRepository;
import com.gary.domain.service.user.UserService;
import com.gary.web.exception.*;
import com.gary.infrastructure.security.JwtTokenUtil;
import com.gary.web.dto.loginResponse.LoginResponseDto;
import com.gary.web.dto.user.UserRequest;
import com.gary.web.dto.user.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
    private final RefreshTokenServiceImpl tokenService;
    private final MetricIncrement metricIncrement;


    @Override
    @Transactional
    @LoggableAction("User Register Token")
    @Timed("user.register.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "fallbackRegister")
    public UserResponse register(UserRequest userRequest) {

        if (userRepository.findByUsername(userRequest.username()).isPresent()) {
            log.warn("Timestamp='{}' Registration failed: username '{}' already exists",
                    TimeFormat.nowTimestamp(),
                    userRequest.username());
            throw new DuplicateResourceException("Username already exists");
        }

        User user = userRepository.save(User.builder()
                .username(userRequest.username())
                .password(passwordEncoder.encode(userRequest.password()))
                .createdAt(LocalDateTime.now())
                .build());

        metricIncrement.incrementMetric("user.register", "success");
        return UserResponse.fromEntity(user);
    }

    UserResponse fallbackRegister(UserRequest userRequest, Throwable e) {
        metricIncrement.incrementMetric("user.register", "fallback");
        log.error("Timestamp='{}' Register fallback triggered: {}",
                TimeFormat.nowTimestamp(),
                e.toString());

        throw new ServiceUnavailableException("Registration service temporarily unavailable. Try again later.");
    }

    @Override
    @Transactional
    @LoggableAction("User Search By Username Token")
    @Timed("user.searchByUsername.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "searchByUsernameFallback")
    public List<UserResponse> searchByUsername(String username, UUID requesterId) {

        return userRepository.findByUsernameContainingIgnoreCase(username).stream()
                .filter(user -> !user.getId().equals(requesterId))
                .map(UserResponse::fromEntity)
                .toList();
    }

    List<UserResponse> searchByUsernameFallback(String username, UUID requesterId, Throwable e) {
        log.error("Timestamp='{}' Search failed for username='{}' by requesterId={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                username,
                requesterId,
                e.toString());

        return Collections.emptyList();
    }

    @Override
    @LoggableAction("User Login")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "loginFallback")
    public LoginResponseDto login(UserRequest userRequest) {

        User user = userRepository.findByUsername(userRequest.username())
                .orElseThrow(() -> {
                    log.warn("Timestamp='{}' Login failed: username '{}' not found",
                            TimeFormat.nowTimestamp(),
                            userRequest.username());
                    metricIncrement.incrementMetric("user.login", "not_found");
                    return new UnauthorizedException("Invalid credentials");
                });

        if (!passwordEncoder.matches(userRequest.password(), user.getPassword())) {
            metricIncrement.incrementMetric("user.login", "invalid_password");
            log.warn("Timestamp='{}' Login failed: invalid password for username '{}'",
                    TimeFormat.nowTimestamp(),
                    userRequest.username());
            throw new UnauthorizedException("Invalid credentials");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = tokenService.create(user.getId()).getToken();

        if (refreshToken == null) {
            throw new ServiceUnavailableException("Failed to create refresh token. Please try again later.");
        }

        metricIncrement.incrementMetric("user.login", "success");

        return LoginResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    LoginResponseDto fallbackLogin(UserRequest userRequest, Throwable e) {
        metricIncrement.incrementMetric("user.login", "fallback");
        log.error("Timestamp='{}' Login fallback triggered: {}", TimeFormat.nowTimestamp(), e.toString());
        throw new ServiceUnavailableException("Login service temporarily unavailable. Try again later.");
    }

    @Override
    @LoggableAction("User Logout")
    public boolean logout(User user) {

        boolean success = tokenService.deleteByUser(user.getId());

        if (!success) {
            throw new ServiceUnavailableException("Failed to log out. Please try again later.");
        }

        return true;
    }

    @Override
    @Transactional
    @LoggableAction("User Refresh Token")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "fallbackRefreshToken")
    public LoginResponseDto refreshToken(String token) {

        //new refresh token is not returned on purpose

        RefreshTokenResponse verifiedTokenResponse = tokenService.verify(token);

        if (verifiedTokenResponse.resultStatus() == ResultStatus.FALLBACK) {
            throw new ServiceUnavailableException("Token refresh service temporarily unavailable.");
        }


        if (verifiedTokenResponse.resultStatus() == ResultStatus.MISS) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        UUID userId = verifiedTokenResponse.refreshToken().getUserId();
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new UnauthorizedException("User not found");
        }

        User user = userOptional.get();

        String newAccessToken = jwtTokenUtil.generateAccessToken(userId, user.getUsername());

        return LoginResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(null)
                .build();
    }


    LoginResponseDto fallbackRefreshToken(String token, Throwable e) {
        log.error("Timestamp='{}' Refresh fallback triggered: {}", TimeFormat.nowTimestamp(), e.toString());
        throw new ServiceUnavailableException("Token refresh service temporarily unavailable.");
    }

    @Override
    @LoggableAction("User Get By Id")
    public Optional<User> getById(UUID id) {
        try {
            return userRepository.findById(id);
        } catch (RuntimeException e) {
            log.warn("getById failed: {}", e.getMessage());
            throw new ServiceUnavailableException("Failed to get user by id: " + id);
        }
    }

    @Override
    @LoggableAction("User Find All By Id")
    public List<User> findAllById(List<UUID> userIds) {
        try {
            return userRepository.findAllById(userIds);
        } catch (RuntimeException e) {
            log.warn("findAllById failed: {}", e.getMessage());
            throw new ServiceUnavailableException("Failed to find users by ids");
        }
    }

}

package com.gary.application.user;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.common.metric.MetricIncrement;
import com.gary.common.ResultStatus;
import com.gary.common.time.TimeFormat;
import com.gary.application.token.RefreshTokenResponse;
import com.gary.application.token.RefreshTokenServiceImpl;
import com.gary.domain.model.user.User;
import com.gary.domain.repository.jpa.user.UserRepository;
import com.gary.domain.service.user.UserService;
import com.gary.infrastructure.jwt.JwtTokenUtil;
import com.gary.web.dto.rest.loginResponse.LoginResponseDto;
import com.gary.web.dto.rest.user.UserRequest;
import com.gary.web.dto.rest.user.UserResponse;
import com.gary.web.exception.rest.DuplicateResourceException;
import com.gary.web.exception.rest.ServiceUnavailableException;
import com.gary.web.exception.rest.UnauthorizedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.JDBCException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
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

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenServiceImpl tokenService;
    private final MetricIncrement metricIncrement;
    private final UserTransactionHelper userTransactionHelper;


    @Override
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.SERIALIZABLE
    )
    @LoggableAction("User Register")
    @Timed("user.register.duration")
//    @Retryable(value = SQLException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public UserResponse register(UserRequest userRequest) {

        log.info("Register class proxy: {}", this.getClass());

        if (userTransactionHelper.findByUsername(userRequest.username()).isPresent()) {
            throw new DuplicateResourceException("username is exist");
        }

        User user = User.builder()
                .username(userRequest.username())
                .password(passwordEncoder.encode(userRequest.password()))
                .createdAt(LocalDateTime.now())
                .build();

        try {

            user = userTransactionHelper.save(user);

        } catch (DataIntegrityViolationException e){

            log.error("Timestamp='{}' Registration failed for username='{}'. Cause: {}",
                    TimeFormat.nowTimestamp(),
                    userRequest.username(),
                    e.toString());

            metricIncrement.incrementMetric("user.register", "fail");

            throw new DuplicateResourceException("username is exist");

        }


        metricIncrement.incrementMetric("user.register", "success");
        return UserResponse.fromEntity(user);

    }


    @Override
    @Transactional(
            readOnly = true,
            propagation = Propagation.SUPPORTS,
            isolation = Isolation.READ_COMMITTED
    )
    @LoggableAction("User Search By Username")
    @Timed("user.searchByUsername.duration")
//    @Retryable(value = SQLException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public List<UserResponse> searchByUsername(String username, UUID requesterId) {

        return userTransactionHelper.findByUsername(username).stream()
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
    @Transactional(
            readOnly = true,
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED)
    //    @Retryable(value = SQLException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public LoginResponseDto login(UserRequest userRequest) {

        User user = userTransactionHelper.findByUsername(userRequest.username())
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

    LoginResponseDto loginFallback(UserRequest userRequest, Throwable e) {
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
    @Transactional()
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
        Optional<User> userOptional = getById(userId);

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
            return userTransactionHelper.findById(id);
        } catch (RuntimeException e) {
            log.warn("getById failed: {}", e.getMessage());
            throw new ServiceUnavailableException("Failed to get user by id: " + id);
        }
    }

    @Override
    @Transactional(
            readOnly = true,
            propagation = Propagation.SUPPORTS)
    @LoggableAction("User Find All By Id")
    public List<User> findAllById(List<UUID> userIds) {
        try {
            return userTransactionHelper.findAllById(userIds);
        } catch (RuntimeException e) {
            log.warn("findAllById failed: {}", e.getMessage());
            throw new ServiceUnavailableException("Failed to find users by ids");
        }
    }

}

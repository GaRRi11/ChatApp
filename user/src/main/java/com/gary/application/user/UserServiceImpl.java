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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
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

        return UserResponse.fromEntity(user);
    }

    @Override
    public List<UserResponse> searchByUsername(String username, Long requesterId) {
        log.info("Searching users by username='{}', requested by userId={}", username, requesterId);
        return userRepository.findByUsernameContainingIgnoreCase(username).stream()
                .filter(user -> !user.getId().equals(requesterId))
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    public LoginResponseDto login(UserRequest userRequest) {
        log.info("Login attempt: username={}", userRequest.username());

        User user = userRepository.findByUsername(userRequest.username())
                .orElseThrow(() -> {
                    log.warn("Login failed: username '{}' not found", userRequest.username());
                    return new UnauthorizedException("Invalid credentials");
                });

        if (!passwordEncoder.matches(userRequest.password(), user.getPassword())) {
            log.warn("Login failed: invalid password for username '{}'", userRequest.username());
            throw new UnauthorizedException("Invalid credentials");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());

        refreshTokenService.save(user.getId(), refreshToken);

        log.info("Login successful: userId={}, username={}", user.getId(), user.getUsername());

        return LoginResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(User user) {
        log.info("Logout requested: userId={}, username={}", user.getId(), user.getUsername());

        refreshTokenService.revokeAll(user.getId());

        log.info("Logout successful: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional
    public LoginResponseDto refreshToken(String token) {
        if (!jwtTokenUtil.validateToken(token)) {
            log.warn("Refresh token validation failed: reason=invalid JWT");
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (!refreshTokenService.isValid(token)) {
            log.warn("Refresh token validation failed: reason=revoked or expired");
            throw new UnauthorizedException("Invalid refresh token");
        }

        Long userId = jwtTokenUtil.extractUserId(token);
        String username = jwtTokenUtil.extractUsername(token);

        log.info("Refreshing access token for userId={}, username={}", userId, username);

        refreshTokenService.revoke(token); // revoke old token
        String newAccessToken = jwtTokenUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, username);
        refreshTokenService.save(userId, newRefreshToken);

        log.info("Refresh token succeeded: userId={}, username={}", userId, username);

        return LoginResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public Optional<User> getById(Long id) {
        log.debug("Fetching user by ID: userId={}", id);
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAllById(List<Long> userIds) {
        log.debug("Fetching multiple users by IDs: count={}", userIds.size());
        return userRepository.findAllById(userIds);
    }

}

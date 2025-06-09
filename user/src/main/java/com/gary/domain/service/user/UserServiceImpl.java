package com.gary.domain.service.user;

import com.gary.domain.model.user.User;
import com.gary.domain.repository.UserRepository;
import com.gary.domain.service.presence.UserPresenceService;
import com.gary.exceptions.DuplicateResourceException;
import com.gary.exceptions.UnauthorizedException;
import com.gary.infrastructure.security.JwtTokenUtil;
import com.gary.web.dto.loginResponse.LoginResponseDto;
import com.gary.web.dto.user.UserRequest;
import com.gary.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserPresenceService userPresenceService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserResponse register(UserRequest userRequest) {

        log.info("Registering new user: {}", userRequest.username());

        userRepository.findByUsername(userRequest.username()).ifPresent(u -> {
            log.warn("Username '{}' already exists", userRequest.username());
            throw new DuplicateResourceException("Username already exists");
        });

        User user = userRepository.save(User.builder()
                .username(userRequest.username())
                .password(passwordEncoder.encode(userRequest.password()))
                .build());

        UserResponse userResponse = UserResponse.fromEntity(user);

        log.info("User '{}' registered successfully", userRequest.username());

        return userResponse;
    }

    @Override
    public LoginResponseDto login(UserRequest userRequest) {

        log.info("Attempting login for user: {}", userRequest.username());

        User user = userRepository.findByUsername(userRequest.username())
                .orElseThrow(() -> {
                    log.warn("Login failed: username '{}' not found", userRequest.username());
                    return new UnauthorizedException("Invalid credentials");
                });

        if (!passwordEncoder.matches(userRequest.password(), user.getPassword())) {
            log.warn("Login failed: invalid password for user '{}'", userRequest.username());
            throw new UnauthorizedException("Invalid credentials");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());

        refreshTokenService.save(user.getId(), refreshToken);

        log.info("User '{}' logged in successfully", userRequest.username());

        return LoginResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    @Override
    public LoginResponseDto refreshToken(String token) {

        log.info("Refreshing access token");

        if (!jwtTokenUtil.validateToken(token)) {
            log.warn("Refresh token validation failed (invalid JWT)");
            throw new UnauthorizedException("Invalid refresh token");
        }

        if (!refreshTokenService.isValid(token)) {
            log.warn("Refresh token validation failed (expired or revoked)");
            throw new UnauthorizedException("Invalid refresh token");
        }

        Long userId = jwtTokenUtil.extractUserId(token);
        String username = jwtTokenUtil.extractUsername(token);

        String newAccessToken = jwtTokenUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, username);

        refreshTokenService.save(userId, newRefreshToken);

        log.info("New tokens issued for user '{}'", username);

        return LoginResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


    @Override
    public Optional<User> getById(Long id) {
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> u.setOnline(userPresenceService.isOnline(u.getId())));
        return user;
    }

    @Override
    public List<User> findAllById(List<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);

        // Update online status for each user
        users.forEach(user -> user.setOnline(userPresenceService.isOnline(user.getId())));

        return users;
    }


}

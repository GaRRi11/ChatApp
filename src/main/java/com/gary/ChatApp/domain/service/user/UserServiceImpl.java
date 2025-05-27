package com.gary.ChatApp.domain.service.user;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.repository.UserRepository;
import com.gary.ChatApp.domain.service.userPresenceService.UserPresenceService;
import com.gary.ChatApp.exceptions.DuplicateResourceException;
import com.gary.ChatApp.exceptions.UnauthorizedException;
import com.gary.ChatApp.security.JwtTokenUtil;
import com.gary.ChatApp.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

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
    public void register(String username, String password) {

        log.info("Registering new user: {}", username);

        userRepository.findByName(username).ifPresent(u -> {
            log.warn("Username '{}' already exists", username);
            throw new DuplicateResourceException("Username already exists");
        });

        userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build());

        log.info("User '{}' registered successfully", username);

    }

    @Override
    public LoginResponse login(String username, String password) {

        log.info("Attempting login for user: {}", username);

        User user = userRepository.findByName(username)
                .orElseThrow(() -> {
                    log.warn("Login failed: username '{}' not found", username);
                    return new UnauthorizedException("Invalid credentials");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed: invalid password for user '{}'", username);
            throw new UnauthorizedException("Invalid credentials");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), user.getUsername());

        refreshTokenService.save(user.getId(), refreshToken);

        log.info("User '{}' logged in successfully", username);

        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    @Override
    public LoginResponse refreshToken(String token) {

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

        return LoginResponse.builder()
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
    public Optional<User> getByName(String name) {
        Optional<User> user = userRepository.findByName(name);
        user.ifPresent(u -> u.setOnline(userPresenceService.isOnline(u.getId())));
        return user;
    }

}

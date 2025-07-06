package com.gary.application.user;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.common.time.TimeFormat;
import com.gary.domain.model.token.RefreshToken;
import com.gary.domain.model.user.User;
import com.gary.domain.service.refreshToken.RefreshTokenService;
import com.gary.domain.service.user.UserService;
import com.gary.infrastructure.jwt.JwtTokenUtil;
import com.gary.web.dto.rest.loginResponse.LoginResponseDto;
import com.gary.web.dto.rest.user.UserRequest;
import com.gary.web.dto.rest.user.UserResponse;
import com.gary.web.exception.rest.DuplicateResourceException;
import com.gary.web.exception.rest.ServiceUnavailableException;
import com.gary.web.exception.rest.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService tokenService;
    private final UserTransactionHelper userTransactionHelper;


    @Override
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.SERIALIZABLE
    )
    @LoggableAction("User Register")
    @Timed("user.register.duration")
    public UserResponse register(UserRequest userRequest) {

        if (userTransactionHelper.findByUsername(userRequest.username()).isPresent()) {
            throw new DuplicateResourceException("username exists");
        }

        User user = User.builder()
                .username(userRequest.username())
                .password(passwordEncoder.encode(userRequest.password()))
                .createdAt(LocalDateTime.now())
                .build();

        try {

            user = userTransactionHelper.save(user);

        } catch (DataIntegrityViolationException e) {

            log.error("Registration failed for username='{}'. Cause: {}",
                    userRequest.username(),
                    e.toString());


            throw new ServiceUnavailableException("Registration Unavailable, Try Again Later");
        }


        return UserResponse.fromEntity(user);

    }


    @Override
    @Transactional(
            readOnly = true,
            propagation = Propagation.SUPPORTS,
            isolation = Isolation.READ_COMMITTED
    )
    public List<UserResponse> searchByUsername(String username) {
        return userTransactionHelper.findByUsername(username).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    @LoggableAction("User Login")
    @Transactional(
            readOnly = true,
            propagation = Propagation.REQUIRED,
            isolation = Isolation.READ_COMMITTED)
    public LoginResponseDto login(UserRequest userRequest) {

        Optional<User> optUser = userTransactionHelper.findByUsername(userRequest.username());

        if (optUser.isEmpty()) {
            log.warn("Login failed: username '{}' not found",
                    userRequest.username());
            throw new UnauthorizedException("Invalid Credentials");
        }

        User user = optUser.get();

        if (!passwordEncoder.matches(userRequest.password(), user.getPassword())) {
            log.warn("Login failed: invalid password for username '{}'",
                    userRequest.username());
            throw new UnauthorizedException("Invalid Credentials");
        }

        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), user.getUsername());

        Optional<RefreshToken> optionalToken = tokenService.findByUserId(user.getId());

        String refreshToken;

        if (optionalToken.isEmpty()) {

            refreshToken = tokenService.create(user.getId()).getToken();

            return LoginResponseDto.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        RefreshToken refreshTokenObject = optionalToken.get();

        if (!tokenService.verifyExpiration(refreshTokenObject)) {

            refreshToken = tokenService.create(user.getId()).getToken();

            return LoginResponseDto.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        refreshToken = refreshTokenObject.getToken();

        return LoginResponseDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @LoggableAction("User Logout")
    public void logout(UUID userId) {
        tokenService.deleteByUser(userId);
    }

    @Override
    @Transactional()
    @LoggableAction("User Refresh Token")
    public LoginResponseDto refreshToken(String token) {

        Optional<RefreshToken> optionalToken = tokenService.getTokenObject(token);

        if (optionalToken.isEmpty()) {
            throw new UnauthorizedException("Refresh token is not in database!");
        }

        RefreshToken refreshToken = optionalToken.get();

        if (!tokenService.verifyExpiration(refreshToken)) {
            throw new UnauthorizedException("Token Is Expired");
        }

        UUID userId = refreshToken.getUserId();

        Optional<User> optionalUser = findById(userId);

        if (optionalUser.isEmpty()) {
            throw new UnauthorizedException("Invalid Token");
        }

        User user = optionalUser.get();

        String newAccessToken = jwtTokenUtil.generateAccessToken(userId, user.getUsername());

        return LoginResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(null)
                .build();
    }


    @Override
    public Optional<User> findById(UUID id) {
        return userTransactionHelper.findById(id);
    }

    @Override
    @Transactional(
            readOnly = true,
            propagation = Propagation.SUPPORTS)
    public List<User> findAllById(List<UUID> userIds) {
        return userTransactionHelper.findAllById(userIds);
    }

}

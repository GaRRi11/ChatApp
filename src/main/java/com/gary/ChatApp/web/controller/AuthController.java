package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.repository.UserRepository;
import com.gary.ChatApp.domain.service.user.RefreshTokenService;
import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.exceptions.DuplicateResourceException;
import com.gary.ChatApp.exceptions.UnauthorizedException;
import com.gary.ChatApp.security.JwtTokenUtil;
import com.gary.ChatApp.web.dto.AuthRequest;
import com.gary.ChatApp.web.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService; // optional, for token storage


    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid AuthRequest request) {
        userService.register(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid AuthRequest request) {
        LoginResponse response = userService.login(request.username(), request.password());
        return ResponseEntity.ok(response);

    }


    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token (signature, expiration, etc.)
        if (!jwtTokenUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();  // Unauthorized
        }

        // Optionally check refresh token exists in DB & is valid
        if (refreshTokenService != null && !refreshTokenService.isValid(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = jwtTokenUtil.getUserIdFromToken(refreshToken);
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

        // Generate new access token
        String newAccessToken = jwtTokenUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, username);

        // Optionally update refresh token in DB
        if (refreshTokenService != null) {
            refreshTokenService.save(userId, newRefreshToken);
        }

        LoginResponse response = LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

        return ResponseEntity.ok(response);
    }

}

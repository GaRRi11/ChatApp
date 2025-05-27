package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.repository.UserRepository;
import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.exceptions.DuplicateResourceException;
import com.gary.ChatApp.exceptions.UnauthorizedException;
import com.gary.ChatApp.security.JwtTokenUtil;
import com.gary.ChatApp.web.dto.AuthRequest;
import com.gary.ChatApp.web.dto.LoginResponse;
import com.gary.ChatApp.web.dto.RefreshTokenRequest;
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
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        LoginResponse response = userService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}

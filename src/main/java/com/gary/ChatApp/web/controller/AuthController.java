package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.web.dto.LoginResponse;
import com.gary.ChatApp.web.dto.RefreshTokenDto;
import com.gary.ChatApp.web.dto.user.UserRequest;
import com.gary.ChatApp.web.dto.user.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid UserRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenDto request) {
        LoginResponse response = userService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}

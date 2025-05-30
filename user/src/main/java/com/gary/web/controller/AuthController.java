package com.gary.web.controller;

import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.web.dto.loginResponse.LoginResponseDto;
import com.gary.ChatApp.web.dto.refreshToken.RefreshTokenDto;
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
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid UserRequest request) {
        LoginResponseDto response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDto> refreshToken(@RequestBody @Valid RefreshTokenDto request) {
        LoginResponseDto response = userService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}

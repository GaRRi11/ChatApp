package com.gary.web.controller.auth;

import com.gary.domain.model.user.User;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.rest.loginResponse.LoginResponseDto;
import com.gary.web.dto.rest.refreshToken.RefreshTokenDto;
import com.gary.web.dto.rest.user.UserRequest;
import com.gary.web.dto.rest.user.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User authenticatedUser) {
        UUID  userId = authenticatedUser.getId();
        userService.logout(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDto> refreshToken(@RequestBody @Valid RefreshTokenDto request) {
        LoginResponseDto response = userService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}

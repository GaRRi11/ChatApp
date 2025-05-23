package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.repository.UserRepository;
import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.exceptions.DuplicateResourceException;
import com.gary.ChatApp.exceptions.UnauthorizedException;
import com.gary.ChatApp.security.JwtTokenUtil;
import com.gary.ChatApp.web.dto.AuthRequest;
import com.gary.ChatApp.web.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody @Valid AuthRequest request) {
        if (userRepository.findByName(request.username()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }

        return userService.register(request.username(), request.password());

    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid AuthRequest request) {
        User user = userRepository.findByName(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return userService.login(request.username(), request.password());

    }
}

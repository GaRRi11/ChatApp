package com.gary.ChatApp.web.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank(message = "Username must not be blank")
        @Size(max = 30, message = "Username must be at most 30 characters")
        String username,

        @NotBlank(message = "Password must not be blank")
        @Size(max = 30, message = "Password must be at most 30 characters")
        String password) {}

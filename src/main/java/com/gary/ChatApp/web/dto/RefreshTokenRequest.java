package com.gary.ChatApp.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh Token must not be blank")
        String refreshToken) {
}

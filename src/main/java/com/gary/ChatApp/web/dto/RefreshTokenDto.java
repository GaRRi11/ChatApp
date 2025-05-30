package com.gary.ChatApp.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDto(
        @NotBlank(message = "Refresh Token must not be blank")
        String refreshToken) {
}

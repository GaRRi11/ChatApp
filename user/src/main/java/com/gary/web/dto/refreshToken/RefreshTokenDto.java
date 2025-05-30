package com.gary.web.dto.refreshToken;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDto(
        @NotBlank(message = "Refresh Token must not be blank")
        String refreshToken) {
}

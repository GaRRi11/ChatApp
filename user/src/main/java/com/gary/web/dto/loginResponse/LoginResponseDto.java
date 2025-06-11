package com.gary.web.dto.loginResponse;


import lombok.Builder;
import lombok.Getter;

@Builder
public record LoginResponseDto(
        String token,
        String refreshToken) {}


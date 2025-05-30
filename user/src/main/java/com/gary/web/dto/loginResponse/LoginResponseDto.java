package com.gary.web.dto.loginResponse;


import lombok.Builder;

@Builder
public record LoginResponseDto(
        String token,
        String refreshToken) {}


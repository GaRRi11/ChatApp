package com.gary.web.dto.rest.loginResponse;


import lombok.Builder;
import lombok.Getter;

@Builder
public record LoginResponseDto(
        String token,
        String refreshToken) {}


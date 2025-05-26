package com.gary.ChatApp.web.dto;


import lombok.Builder;

@Builder
public record LoginResponse(
        String token,
        String refreshToken) {}


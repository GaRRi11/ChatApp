package com.gary.ChatApp.web.dto;


import lombok.Builder;

@Builder
public record LoginResponse(
        Long id,
        String token,
        String username) {}


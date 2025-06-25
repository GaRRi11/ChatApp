package com.gary.web.dto.rest.chatMessage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatMessageRequest(
        @NotNull
        @PositiveOrZero
        UUID receiverId,
        @NotBlank
        @Size(max = 500)
        String content
) {}

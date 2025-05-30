package com.gary.ChatApp.web.dto.chatMessage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotNull
        @PositiveOrZero
        Long receiverId,
        @NotBlank
        @Size(max = 500)
        String content
) {}

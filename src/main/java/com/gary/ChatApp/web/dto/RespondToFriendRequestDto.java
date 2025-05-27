package com.gary.ChatApp.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RespondToFriendRequestDto(
        @NotNull(message = "Sender ID must not be blank")
        @PositiveOrZero(message = "Request ID must be a positive number")
        Long requestId,

        @NotNull (message = "Respond status must be provided")
        Boolean accept) {}

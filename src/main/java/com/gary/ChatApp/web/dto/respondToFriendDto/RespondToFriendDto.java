package com.gary.ChatApp.web.dto.respondToFriendDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RespondToFriendDto(
        @NotNull(message = "Sender ID must not be blank")
        @PositiveOrZero(message = "Request ID must be a positive number")
        Long requestId,

        @NotNull (message = "Respond status must be provided")
        Boolean accept) {}

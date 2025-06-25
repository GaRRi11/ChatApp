package com.gary.web.dto.rest.respondToFriendDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record RespondToFriendDto(
        @NotNull(message = "Sender ID must not be blank")
        @PositiveOrZero(message = "Request ID must be a positive number")
        UUID requestId,

        @NotNull (message = "Respond status must be provided")
        Boolean accept) {}

package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record FriendRequestCreate(

        @NotNull(message = "Receiver ID must not be blank")
        @PositiveOrZero(message = "Receiver ID must be a positive number")
        Long receiverId,

        @NotNull(message = "Request status must not be null")
        RequestStatus status
) {}

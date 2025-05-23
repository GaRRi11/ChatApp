package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record FriendRequestDto(
        @NotNull(message = "Sender ID must not be blank")
        @PositiveOrZero(message = "Sender ID must be a positive number")
        Long senderId,
        @NotNull(message = "Receiver ID must not be blank")
        @PositiveOrZero(message = "Receiver ID must be a positive number")
        Long receiverId,
        @NotNull(message = "Request status must not be null")
        RequestStatus status) {

    public static FriendRequestDto fromEntity(FriendRequest friendRequest) {
        return new FriendRequestDto(
                friendRequest.getSenderId(),
                friendRequest.getReceiverId(),
                friendRequest.getStatus()
        );
    }
}

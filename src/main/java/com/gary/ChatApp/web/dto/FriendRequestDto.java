package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;

public record FriendRequestDto(Long senderId, Long receiverId, RequestStatus status) {

    public static FriendRequestDto fromEntity(FriendRequest friendRequest) {
        return new FriendRequestDto(
                friendRequest.getSenderId(),
                friendRequest.getReceiverId(),
                friendRequest.getStatus()
        );
    }
}

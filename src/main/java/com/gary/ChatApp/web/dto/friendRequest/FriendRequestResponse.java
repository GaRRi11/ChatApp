package com.gary.ChatApp.web.dto.friendRequest;

import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FriendRequestResponse(
        Long id,
        Long senderId,
        Long receiverId,
        RequestStatus status,
        LocalDateTime createdAt
) {
    public static FriendRequestResponse fromEntity(com.gary.ChatApp.domain.model.friendrequest.FriendRequest entity) {
        return new FriendRequestResponse(
                entity.getId(),
                entity.getSenderId(),
                entity.getReceiverId(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}

package com.gary.web.dto.friendRequest;

import com.gary.domain.model.friendrequest.FriendRequest;
import com.gary.domain.model.friendrequest.RequestStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FriendRequestResponse(
        UUID id,
        UUID senderId,
        UUID receiverId,
        RequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime respondedAt

) {
    public static FriendRequestResponse fromEntity(FriendRequest entity) {
        return new FriendRequestResponse(
                entity.getId(),
                entity.getSenderId(),
                entity.getReceiverId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getStatus() == RequestStatus.PENDING ? null : entity.getRespondedAt()
        );
    }
}

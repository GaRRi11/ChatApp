package com.gary.web.dto.rest.chatMessage;

import com.gary.domain.model.chatmessage.ChatMessage;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChatMessageResponse(
        UUID id,
        UUID senderId,
        UUID receiverId,
        String content,
        LocalDateTime timestamp
) {
    public static ChatMessageResponse fromEntity(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getSenderId(),
                chatMessage.getReceiverId(),
                chatMessage.getContent(),
                chatMessage.getTimestamp()
        );
    }
}



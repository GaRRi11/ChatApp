package com.gary.web.dto.chatMessage;

import com.gary.domain.model.chatmessage.ChatMessage;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageResponse(
        Long id,
        Long senderId,
        Long receiverId,
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



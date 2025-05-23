package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.domain.model.chatmessage.ChatMessage;
import java.time.LocalDateTime;

public record ChatMessageDto(Long senderId, Long receiverId, String content, LocalDateTime timestamp) {

    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return new ChatMessageDto(
                chatMessage.getSenderId(),
                chatMessage.getReceiverId(),
                chatMessage.getContent(),
                chatMessage.getTimestamp()
        );
    }
}

package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.domain.model.chatmessage.ChatMessage;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageDto(

        Long senderId,
        @NotNull(message = "Receiver ID must not be blank")
        @PositiveOrZero(message = "Receiver ID must be a positive number")
        Long receiverId,
        @NotBlank(message = "Content must not be blank")
        @Size(max = 500, message = "Content must be at most 500 characters")
        String content,
        @NotBlank(message = "Timestamp must not be blank")
        @PastOrPresent(message = "Timestamp cannot be in the future")
        LocalDateTime timestamp) {

    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return new ChatMessageDto(
                chatMessage.getSenderId(),
                chatMessage.getReceiverId(),
                chatMessage.getContent(),
                chatMessage.getTimestamp()
        );
    }
}

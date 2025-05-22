package com.gary.ChatApp.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime timestamp;

}

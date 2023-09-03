package com.gary.ChatApp.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ChatMessageRequest {
    private String content;

    public ChatMessageRequest(String content) {
        this.content = content;
    }

    public ChatMessageRequest() {
    }
}

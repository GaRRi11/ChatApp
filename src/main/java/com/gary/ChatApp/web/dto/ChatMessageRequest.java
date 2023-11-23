package com.gary.ChatApp.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ChatMessageRequest {

    private String content;
    private String sender;
    private String receiver;

    public ChatMessageRequest(String content, String sender, String receiver) {
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
    }



    public ChatMessageRequest() {
    }
}

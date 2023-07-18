package com.gary.ChatApp.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageRequest {

    private String content;
    private String sender;
}

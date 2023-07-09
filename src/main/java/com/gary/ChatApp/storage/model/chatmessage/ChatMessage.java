package com.gary.ChatApp.storage.model.chatmessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChatMessage {

    private String content;

    private String sender;

    private MessageType messageType;
}

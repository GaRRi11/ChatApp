package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageDTOMapper {

    public ChatMessage fromDTO(ChatMessageRequest chatMessageRequest, String sender){
        return new ChatMessage(
                chatMessageRequest.getContent(),
                sender
        );
}
}

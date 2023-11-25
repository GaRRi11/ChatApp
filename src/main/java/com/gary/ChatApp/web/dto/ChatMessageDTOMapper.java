package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageDTOMapper {

    public ChatMessage fromDTO(String content,Long senderId,Long receiverId){
        return new ChatMessage(
                content,
                senderId,
                receiverId
        );
}
}

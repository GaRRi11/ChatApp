package com.gary.ChatApp.web.dto;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageDTOMapper {

    private static Long idCounter = 1L;

    private Long generateId(){
        return idCounter++;
    }

    public ChatMessage fromDTO(ChatMessageRequest chatMessageRequest){
        return new ChatMessage(
                generateId(),
                chatMessageRequest.getContent(),
                chatMessageRequest.getSender()
        );
    }
}

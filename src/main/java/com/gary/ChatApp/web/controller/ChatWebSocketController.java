package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.chatmessage.ChatMessage;
import com.gary.ChatApp.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages") // destination for subscribers
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessageService.sendMessage(
                chatMessage.getSenderId(),
                chatMessage.getReceiverId(),
                chatMessage.getContent()
        );
    }
}

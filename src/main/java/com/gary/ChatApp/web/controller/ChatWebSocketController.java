package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.service.chatMessage.ChatMessageService;
import com.gary.ChatApp.domain.service.userPresenceService.UserPresenceService;
import com.gary.ChatApp.web.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/api/Chat")
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService; // 👈 Add this

    @MessageMapping("/send")
    public void sendMessage(@Valid ChatMessageDto message) {
        if (message.receiverId() == null || message.senderId() == null) return;

        // Save and cache message
        chatMessageService.sendMessage(message.senderId(), message.receiverId(), message.content());

        ChatMessageDto updatedMessage = new ChatMessageDto(
                message.senderId(),
                message.receiverId(),
                message.content(),
                LocalDateTime.now()
        );


        messagingTemplate.convertAndSendToUser(
                message.receiverId().toString(),
                "/queue/messages",
                updatedMessage
        );


    }
}


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


@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService; // 👈 Add this

    @MessageMapping("/send")
    public void sendMessage(@Valid ChatMessageDto message) {
        if (message.getReceiverId() == null || message.getSenderId() == null) return;

        // Save and cache message
        chatMessageService.sendMessage(message.getSenderId(), message.getReceiverId(), message.getContent());

        message.setTimestamp(LocalDateTime.now());

        messagingTemplate.convertAndSendToUser(
                message.getReceiverId().toString(),
                "/queue/messages",
                message
        );


    }
}


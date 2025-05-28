package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.service.chatMessage.ChatMessageService;
import com.gary.ChatApp.web.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/send")
    public void sendMessage(@Payload ChatMessageDto message) {
        if (message.senderId() == null || message.receiverId() == null || message.content() == null) {
            log.warn("Invalid chat message received: {}", message);
            return;
        }

        // Save and cache message, returns saved entity
        var savedMessage = chatMessageService.sendMessage(
                message.senderId(), message.receiverId(), message.content());

        var dtoToSend = ChatMessageDto.fromEntity(savedMessage);

        // Send message to receiver's personal queue
        messagingTemplate.convertAndSendToUser(
                message.receiverId().toString(),
                "/queue/messages",
                dtoToSend
        );
    }

    /**
     * Example HTTP GET endpoint to fetch chat history with cache usage.
     * Could also be adapted for WebSocket if needed.
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {

        if (user1Id == null || user2Id == null || user1Id <= 0 || user2Id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatMessageDto> messages = chatMessageService.getChatHistory(user1Id, user2Id);

        return ResponseEntity.ok(messages);
    }
}

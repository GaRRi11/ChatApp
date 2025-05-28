package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.service.chatMessage.ChatMessageService;
import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.web.dto.ChatMessageDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
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
    public void sendMessage(
            @Payload @Valid ChatMessageDto message,
            @AuthenticationPrincipal User user) {

        if (message.receiverId() == null || message.content() == null) {
            log.warn("Invalid chat message received: {}", message);
            return;
        }


        Long senderId = user.getId();

        ChatMessageDto updatedDto = ChatMessageDto.builder().
                senderId(senderId)
                .receiverId(message.receiverId())
                .content(message.content())
                .build();

        // Save and cache message, returns saved entity
        var savedMessage = chatMessageService.sendMessage(updatedDto);

        var dtoToSend = ChatMessageDto.fromEntity(savedMessage);

        // Send message to receiver's personal queue
        messagingTemplate.convertAndSendToUser(
                updatedDto.receiverId().toString(),
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

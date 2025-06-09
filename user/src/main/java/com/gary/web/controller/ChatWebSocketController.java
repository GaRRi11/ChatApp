package com.gary.web.controller;

import com.gary.domain.model.user.User;
import com.gary.domain.service.chat.ChatMessageService;
import com.gary.web.dto.chatMessage.ChatMessageRequest;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;


    @MessageMapping("/send")
    @SendToUser("/queue/confirm")
    public ChatMessageResponse sendMessage(
            @Payload @Valid ChatMessageRequest request,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            log.warn("Unauthorized attempt to send a chat message");
            return null;
        }

        try {
            ChatMessageResponse response = chatMessageService.sendMessage(request, user.getId());

            messagingTemplate.convertAndSendToUser(
                    request.receiverId().toString(),
                    "/queue/messages",
                    response
            );

            log.info("User {} sent message to user {}: {}", user.getId(), request.receiverId(), response.content());
            return response;

        } catch (Exception e) {
            log.error("Failed to send chat message from user {} to user {}: {}", user.getId(), request.receiverId(), e.getMessage(), e);
            return null;
        }
    }



    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {

        if (user1Id == null || user1Id <= 0 || user2Id == null || user2Id <= 0) {
            log.warn("Invalid user IDs provided for chat history: user1Id={}, user2Id={}", user1Id, user2Id);
            return ResponseEntity.badRequest().build();
        }

        try {
            List<ChatMessageResponse> messages = chatMessageService.getChatHistory(user1Id, user2Id);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching chat history between users {} and {}: {}", user1Id, user2Id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

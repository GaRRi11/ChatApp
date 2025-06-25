package com.gary.web.controller.chat;

import com.gary.domain.model.user.User;
import com.gary.domain.service.chat.ChatMessageService;
import com.gary.web.dto.chatMessage.rest.ChatMessageRequest;
import com.gary.web.dto.chatMessage.rest.ChatMessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;


    @MessageMapping("/send")
    @SendToUser("/queue/confirm")
    @PreAuthorize("isAuthenticated()")
    public ChatMessageResponse sendMessage(
            @Payload @Valid ChatMessageRequest request,
            @AuthenticationPrincipal User user) {

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(
            @AuthenticationPrincipal User user,
            @RequestParam UUID otherUserId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {

        if (otherUserId == null || user.getId().equals(otherUserId)) {
            log.warn("Invalid request to fetch chat history. userId={}, otherUserId={}", user != null ? user.getId() : null, otherUserId);
            return ResponseEntity.badRequest().build();
        }

        limit = Math.min(limit, 100);

        try {
            List<ChatMessageResponse> messages = chatMessageService.getChatHistory(user.getId(), otherUserId, offset, limit);
            log.info("Chat history fetched for user {} with user {} (offset={}, limit={})", user.getId(), otherUserId, offset, limit);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching chat history between users {} and {}: {}", user.getId(), otherUserId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}



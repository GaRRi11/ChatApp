package com.gary.web.controller.chat;

import com.gary.domain.model.user.User;
import com.gary.domain.service.chat.ChatMessageService;
import com.gary.web.dto.rest.chatMessage.ChatMessageRequest;
import com.gary.web.dto.rest.chatMessage.ChatMessageResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
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

        UUID senderId = user.getId();
        ChatMessageResponse response = chatMessageService.sendMessage(request, senderId);
        messagingTemplate.convertAndSendToUser(
                request.receiverId().toString(),
                "/queue/messages",
                response
        );
        return response;
    }


    @GetMapping("/history/{receiverId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(
            @AuthenticationPrincipal User user,
            @PathVariable UUID receiverId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {

        if (offset < 0 || limit <= 0) {
            log.warn("Invalid pagination: offset={}, limit={}", offset, limit);
            return ResponseEntity.badRequest().build();
        }

        limit = Math.min(limit, 100);

        List<ChatMessageResponse> messages = chatMessageService.getChatHistory(user.getId(), receiverId, offset, limit);

        return ResponseEntity.ok(messages);
    }
}



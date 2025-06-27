package com.gary.web.controller.chat;

import com.gary.application.rateLimiter.RateLimiterStatus;
import com.gary.domain.model.user.User;
import com.gary.domain.service.chat.ChatMessageService;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.web.dto.rest.chatMessage.ChatMessageRequest;
import com.gary.web.dto.rest.chatMessage.ChatMessageResponse;
import com.gary.web.exception.rest.ServiceUnavailableException;
import com.gary.web.exception.websocket.TooManyRequestsException;
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
    private final RateLimiterService rateLimiterService;


    @MessageMapping("/send")
    @SendToUser("/queue/confirm")
    @PreAuthorize("isAuthenticated()")
    public ChatMessageResponse sendMessage(
            @Payload @Valid ChatMessageRequest request,
            @AuthenticationPrincipal User user) {

        UUID senderId = user.getId();

        RateLimiterStatus status = rateLimiterService.isAllowedToSend(senderId);

        switch (status) {
            case BLOCKED:
                log.warn("Rate limit exceeded by user {}", senderId);
                throw new TooManyRequestsException("You're sending messages too quickly. Please wait.");

            case UNAVAILABLE:
                log.error("RateLimiter unavailable for user {}", senderId);
                throw new ServiceUnavailableException("Service is temporarily unavailable. Please try again later.");

            case ALLOWED:
            default:
                ChatMessageResponse response = chatMessageService.sendMessage(request, senderId);
                messagingTemplate.convertAndSendToUser(
                        request.receiverId().toString(),
                        "/queue/messages",
                        response
                );
                return response;
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


        List<ChatMessageResponse> messages = chatMessageService.getChatHistory(user.getId(), otherUserId, offset, limit);

        return ResponseEntity.ok(messages);

    }

}



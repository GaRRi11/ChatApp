package com.gary.application.chat;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.service.chat.ChatCacheService;
import com.gary.domain.service.chat.ChatMessageService;
import com.gary.domain.service.chat.ChatPersistenceService;
import com.gary.domain.service.friendship.FriendshipService;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.rest.chatMessage.ChatMessageRequest;
import com.gary.web.dto.rest.chatMessage.ChatMessageResponse;
import com.gary.web.exception.rest.BadRequestException;
import com.gary.web.exception.rest.ResourceNotFoundException;
import com.gary.web.exception.rest.UnauthorizedException;
import com.gary.web.exception.websocket.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatCacheService chatCacheService;
    private final ChatPersistenceService chatPersistenceService;
    private final RateLimiterService rateLimiterService;
    private final UserService userService;
    private final FriendshipService friendshipService;


    @Override
    @LoggableAction("Send Chat Message")
    @Timed("chat.message.send.duration")
    public ChatMessageResponse sendMessage(ChatMessageRequest request, UUID senderId) {

        UUID receiverId = request.receiverId();

        if (senderId.equals(receiverId)) {
            log.warn("Bad request: User {} attempted to send a message to themselves.", senderId);
            throw new BadRequestException("You cannot send a message to yourself.");
        }

        if (userService.findById(receiverId).isEmpty()) {
            log.error("User with id {} not found", receiverId);
            throw new ResourceNotFoundException("User with ID " + receiverId + " not found");
        }

        if (!friendshipService.areFriends(senderId, receiverId)) {
            log.warn("Unauthorized message attempt: User {} tried to message non-friend {}", senderId, receiverId);
            throw new UnauthorizedException("You are not allowed to message this user because you are not friends.");
        }

        if (!rateLimiterService.isAllowedToSend(senderId)) {
            throw new TooManyRequestsException("Rate limit exceeded for user: " + senderId);
        }

        ChatMessage message = buildMessageEntity(request, senderId);
        ChatMessage savedMessage = chatPersistenceService.saveMessage(message);

        ChatMessageResponse response = ChatMessageResponse.fromEntity(savedMessage);
        cacheMessageAsync(response);
        return response;
    }


    @Override
    @LoggableAction("Get Chat History")
    @Timed("chat.message.getHistory.duration")
    public List<ChatMessageResponse> getChatHistory(UUID senderId, UUID receiverId, int offset, int limit) {

        if (userService.findById(receiverId).isEmpty()) {
            log.error("User with id {} not found", receiverId);
            throw new ResourceNotFoundException("User with ID " + receiverId + " not found");
        }

        if (senderId.equals(receiverId)) {
            log.warn("User {} attempted to fetch chat history with themselves.", senderId);
            throw new BadRequestException("Cannot fetch chat history with yourself.");
        }

        List<ChatMessageResponse> cacheResult = chatCacheService.getCachedMessages(senderId, receiverId, offset, limit);

        if (cacheResult != null && !cacheResult.isEmpty()) {
            return cacheResult;
        }

        List<ChatMessage> persistedResult = chatPersistenceService.findChatBetweenUsers(senderId, receiverId, offset, limit);

        if (persistedResult.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatMessageResponse> responses = persistedResult.stream()
                .map(ChatMessageResponse::fromEntity)
                .toList();

        chatCacheService.saveAll(responses);


        return responses;
    }


    @Async("taskExecutor")
    public void cacheMessageAsync(ChatMessageResponse response) {
        chatCacheService.save(response);
    }

    private ChatMessage buildMessageEntity(ChatMessageRequest request, UUID senderId) {
        return ChatMessage.builder()
                .senderId(senderId)
                .receiverId(request.receiverId())
                .content(request.content().trim())
                .timestamp(LocalDateTime.now())
                .build();
    }

}





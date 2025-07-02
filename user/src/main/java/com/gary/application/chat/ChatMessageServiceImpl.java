package com.gary.application.chat;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.service.chat.ChatCacheService;
import com.gary.domain.service.chat.ChatMessageService;
import com.gary.domain.service.chat.ChatPersistenceService;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.web.dto.rest.chatMessage.ChatMessageRequest;
import com.gary.web.dto.rest.chatMessage.ChatMessageResponse;
import com.gary.web.exception.websocket.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatCacheService chatCacheService;
    private final ChatPersistenceService chatPersistenceService;
    private final RateLimiterService rateLimiterService;


    @Override
    @LoggableAction("Send Chat Message")
    @Timed("chat.message.send.duration")
    public ChatMessageResponse sendMessage(ChatMessageRequest request, UUID senderId) {

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
    public List<ChatMessageResponse> getChatHistory(UUID user1Id, UUID user2Id, int offset, int limit) {

        List<ChatMessageResponse> cacheResult = chatCacheService.getCachedMessages(user1Id, user2Id, offset, limit);

        if (cacheResult != null && !cacheResult.isEmpty()) {
            return cacheResult;
        }

        List<ChatMessage> persistedResult = chatPersistenceService.findChatBetweenUsers(user1Id, user2Id, offset, limit);

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





package com.gary.application.chat;

import com.gary.common.ResultStatus;
import com.gary.common.time.TimeFormat;
import com.gary.application.rateLimiter.RateLimiterStatus;
import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.repository.chatMessage.cache.ChatMessageCacheRepository;
import com.gary.domain.service.chat.ChatCacheService;
import com.gary.domain.service.chat.ChatMessageService;
import com.gary.domain.service.chat.ChatPersistenceService;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.web.dto.chatMessage.cache.ChatMessageCacheDto;
import com.gary.web.exception.ServiceUnavailableException;
import com.gary.web.exception.TooManyRequestsException;
import com.gary.web.dto.chatMessage.rest.ChatMessageRequest;
import com.gary.web.dto.chatMessage.rest.ChatMessageResponse;
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

    private final RateLimiterService rateLimiterService;
    private final ChatCacheService chatCacheService;
    private final ChatPersistenceService chatPersistenceService;


    @Override
    @LoggableAction("Send Chat Message")
    @Timed("chat.message.send.duration")
    public ChatMessageResponse sendMessage(ChatMessageRequest request, UUID senderId) {


        RateLimiterStatus status = rateLimiterService.isAllowedToSend(senderId);

        switch (status) {
            case ALLOWED:

                ChatMessage message = buildMessageEntity(request, senderId);
                ChatMessage savedMessage = chatPersistenceService.saveMessage(message);

                if (savedMessage == null) {
                    throw new ServiceUnavailableException("Message service is temporarily unavailable. Please try again later.");
                }

                ChatMessageResponse response = ChatMessageResponse.fromEntity(savedMessage);
                cacheMessageAsync(response);
                return response;

            case BLOCKED:

                throw new TooManyRequestsException("You're sending messages too quickly. Please wait.");

            case UNAVAILABLE:
            default:

                throw new ServiceUnavailableException("Message service is temporarily unavailable. Please try again later.");
        }
    }


    @Override
    @LoggableAction("Get Chat History")
    @Timed("chat.message.getHistory.duration")
    public List<ChatMessageResponse> getChatHistory(UUID user1Id, UUID user2Id, int offset, int limit) {

        CachedMessagesResult cacheResult = chatCacheService.getCachedMessages(user1Id, user2Id, offset, limit);

        if (cacheResult.status() == ResultStatus.HIT) {
            return cacheResult.messages();
        }

        PersistedMessageResult persistedResult = chatPersistenceService.findChatBetweenUsers(user1Id, user2Id, offset, limit);

        if (persistedResult.status() == ResultStatus.FALLBACK) {
            throw new ServiceUnavailableException("Message service is temporarily unavailable. Please try again later.");
        }

        List<ChatMessageResponse> responses = persistedResult.messages().stream()
                .map(ChatMessageResponse::fromEntity)
                .toList();

        if (cacheResult.status() == ResultStatus.FALLBACK) {
            log.warn("Timestamp='{}' Cache fallback detected. Skipping cache update for users [{} <-> {}].",
                    TimeFormat.nowTimestamp(),
                    user1Id,
                    user2Id);
            return responses;
        }

        if (cacheResult.status() == ResultStatus.MISS && !responses.isEmpty()) {
            responses.forEach(chatCacheService::save);
        }

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





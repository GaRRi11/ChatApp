package com.gary.application.chat;

import com.gary.application.cache.chat.CachedMessagesResult;
import com.gary.application.cache.rateLimiter.RateLimiterStatus;
import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.repository.chatMessage.ChatMessageRepository;
import com.gary.domain.service.cache.ChatCacheService;
import com.gary.domain.service.chat.ChatMessageService;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.web.exception.RateLimiterServiceUnavailableException;
import com.gary.web.exception.TooManyRequestsException;
import com.gary.web.dto.chatMessage.ChatMessageRequest;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import com.gary.annotations.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final RateLimiterService rateLimiterService;
    private final ChatCacheService chatCacheService;
    private final MeterRegistry meterRegistry;

    private static final String CACHE_CB = "chatCacheCircuitBreaker";
    private static final String DB_CB = "chatDbCircuitBreaker";


    @Override
    @LoggableAction("Send Chat Message")
    @Timed("chat.message.send.duration")
    public ChatMessageResponse sendMessage(ChatMessageRequest request, UUID senderId) {

        RateLimiterStatus status = rateLimiterService.isAllowedToSend(senderId);

        switch (status) {
            case ALLOWED:

                ChatMessage message = buildMessageEntity(request, senderId);
                ChatMessage savedMessage = saveMessageWithRetryAndCircuitBreaker(message);
                ChatMessageResponse response = ChatMessageResponse.fromEntity(savedMessage);
                cacheMessageAsync(response);
                return response;

            case BLOCKED:

                log.warn("User {} is blocked by rate limiter due to sending messages too quickly.", senderId);
                throw new TooManyRequestsException("You're sending messages too quickly. Please wait.");

            case UNAVAILABLE:
            default:

                log.error("Rate limiter service unavailable for user {}. Throwing exception.", senderId);
                throw new RateLimiterServiceUnavailableException("Message service is temporarily unavailable. Please try again later.");
        }
    }

    @Async("taskExecutor")
    public void cacheMessageAsync(ChatMessageResponse response) {
        chatCacheService.cacheMessage(response);
    }

    private ChatMessage buildMessageEntity(ChatMessageRequest request, UUID senderId) {
        return ChatMessage.builder()
                .senderId(senderId)
                .receiverId(request.receiverId())
                .content(request.content().trim())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Retry
    @CircuitBreaker(name = DB_CB, fallbackMethod = "dbSaveFallback")
    public ChatMessage saveMessageWithRetryAndCircuitBreaker(ChatMessage message) {
        try {
            ChatMessage saved = chatMessageRepository.save(message);
            log.info("Message saved to DB: id={}, senderId={}, receiverId={}",
                    saved.getId(), message.getSenderId(), message.getReceiverId());
            return saved;
        } catch (DataAccessException e) {
            meterRegistry.counter("chat.message.db.saveRetries").increment();
            log.error("DB save failed, retrying... cause: {}", e.getMessage());
            throw e;  // triggers retry
        }
    }

    // Fallback in case DB save circuit breaker opens
    public ChatMessage dbSaveFallback(ChatMessage message, Throwable t) {
        log.error("DB save operation failed permanently: {}", t.toString());
        // Optional: alerting or fallback logic here (e.g., queue for retry later)
        throw new RuntimeException("Database temporarily unavailable, please try again later.");
    }


    @Override
    @LoggableAction("Get Chat History")
    @Timed("chat.message.getHistory.duration")
    public List<ChatMessageResponse> getChatHistory(UUID user1Id, UUID user2Id, int offset, int limit) {

        List<ChatMessageResponse> cachedMessages = null;

        try {
            cachedMessages = getCachedMessagesAsync(user1Id, user2Id, offset, limit);
        } catch (Exception e) {
            log.warn("Cache read failed: {}", e.getMessage());
            meterRegistry.counter("chat.message.cache.failures").increment();
            // fallback to DB
        }

        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            log.info("Cache HIT for chat between {} and {}", user1Id, user2Id);
            return cachedMessages;
        }

        log.info("Cache MISS for chat between {} and {}", user1Id, user2Id);

        // Read from DB without retries (could add if needed)

        //IF YOU SEE THIS COMMENT ADD LOGIC FOR WHEN CACHE MISSED THEN RETREIVE MESSAGES FROM DB AND RECACHE THEM
        List<ChatMessage> messages = chatMessageRepository.findChatBetweenUsers(user1Id, user2Id, offset, limit);

        List<ChatMessageResponse> responses = messages.stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());

        // Async caching of results
        responses.forEach(this::cacheMessageAsync);

        CachedMessagesResult result = chatCacheService.getCachedMessages(user1Id, user2Id, offset, limit);

        if (!result.fallbackUsed() && !result.messages().isEmpty()) {
            return result.messages();
        } else if (!result.fallbackUsed()) {
            List<ChatMessage> messages = chatMessageRepository.findChatBetweenUsers(user1Id, user2Id, offset, limit);

            List<ChatMessageResponse> responses = messages.stream()
                    .map(ChatMessageResponse::fromEntity)
                    .collect(Collectors.toList());

            if (!messagesFromDb.isEmpty()) {
                responses.forEach(this::cacheMessageAsync);
            }
            return messagesFromDb;
        } else {
            log.warn("Cache unavailable. Fetching from DB, skipping cache update.");


            List<ChatMessage> messages = chatMessageRepository.findChatBetweenUsers(user1Id, user2Id, offset, limit);

            List<ChatMessageResponse> responses = messages.stream()
                    .map(ChatMessageResponse::fromEntity)
                    .collect(Collectors.toList());

            return responses;

        }
    }

    @Async
    public CompletableFuture<List<ChatMessageResponse>> getCachedMessagesAsync(UUID user1Id, UUID user2Id, int offset, int limit) {
        return CompletableFuture.completedFuture(chatCacheService.getCachedMessages(user1Id, user2Id, offset, limit));
    }

}


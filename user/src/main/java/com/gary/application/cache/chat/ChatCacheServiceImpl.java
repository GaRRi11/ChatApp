package com.gary.application.cache.chat;

import com.gary.infrastructure.constants.RedisKeys;
import com.gary.domain.service.cache.ChatCacheService;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import com.gary.annotations.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatCacheServiceImpl implements ChatCacheService {

    private final RedisTemplate<String, ChatMessageResponse> chatMessageRedisTemplate;
    private static final Duration MESSAGE_EXPIRATION = Duration.ofHours(6);
    private final MeterRegistry meterRegistry;


    @Override
    @Timed("chat.cache.cacheMessage.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "cacheMessageFallback")
    @LoggableAction("Is Allowed To Send")
    public void cacheMessage(ChatMessageResponse message) {

        String key = RedisKeys.chatMessages(message.senderId(), message.receiverId());

        boolean isNewKey = !chatMessageRedisTemplate.hasKey(key);

        chatMessageRedisTemplate.opsForList().rightPush(key, message);

        if (isNewKey) {
            chatMessageRedisTemplate.expire(key, MESSAGE_EXPIRATION);
            log.info("New Redis key created: {}. Expiration set to {} hours", key, MESSAGE_EXPIRATION.toHours());
        }

        meterRegistry.counter("chat.message.cache", "status", "success").increment();
    }

    @Override
    @LoggableAction("Cache Fallback")
    public void cacheFallback(ChatMessageResponse response, Throwable t) {
        log.warn("Failed to cache message id={} after retries/circuit breaker: {}", response.id(), t.getMessage());
        meterRegistry.counter("chat.message.cache", "status", "fallback").increment();
    }


    @Override
    @LoggableAction("Retrieve Cached Messages")
    @Timed("chat.cache.getMessages.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getCachedMessagesFallback")
    public CachedMessagesResult getCachedMessages(UUID user1Id, UUID user2Id, int offset, int limit) {
        String key = RedisKeys.chatMessages(user1Id, user2Id);

        List<ChatMessageResponse> messages = chatMessageRedisTemplate.opsForList().range(key, offset, offset + limit - 1);

        if (messages == null || messages.isEmpty()) {
            log.info("No cached messages found for users [{} <-> {}] with offset={} and limit={}",
                    user1Id, user2Id, offset, limit);
            meterRegistry.counter("chat.message.cache", "status", "miss").increment();
        } else {
            log.info("Retrieved {} cached messages for users [{} <-> {}] with offset={} and limit={}",
                    messages.size(), user1Id, user2Id, offset, limit);
            meterRegistry.counter("chat.message.cache", "status", "hit").increment();
        }

        CachedMessagesResult result = CachedMessagesResult.builder()
                .messages(messages == null ? List.of() : messages)
                .fallbackUsed(false)
                .build();

        return result;
    }


    @Override
    @LoggableAction("Get Cached Messages Fallback")
    public CachedMessagesResult getCachedMessagesFallback(UUID user1Id, UUID user2Id, int offset, int limit, Throwable t) {
        log.warn("Failed to get cached messages for users [{} <-> {}]: {}", user1Id, user2Id, t.getMessage());
        meterRegistry.counter("chat.message.cache", "status", "fallback").increment();

        CachedMessagesResult result = CachedMessagesResult.builder()
                .messages(List.of())
                .fallbackUsed(true)
                .build();

        return result;
    }

    @Override
    @LoggableAction("Clear Cached Messages")
    @Timed("chat.cache.clearMessages.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "clearCachedMessagesFallback")
    public void clearCachedMessages(UUID user1Id, UUID user2Id) {
        String key = RedisKeys.chatMessages(user1Id, user2Id);

        Boolean deleted = chatMessageRedisTemplate.delete(key);

        if (deleted) {
            log.info("Evicted chat cache for users [{} <-> {}] (key={})", user1Id, user2Id, key);
        } else {
            log.warn("Attempted to evict chat cache for users [{} <-> {}], but key was not found (key={})",
                    user1Id, user2Id, key);
        }
    }

    @Override
    public void clearCachedMessagesFallback(UUID user1Id, UUID user2Id, Throwable t) {
        log.warn("Failed to clear cached messages for users [{} <-> {}]: {}", user1Id, user2Id, t.getMessage());
        meterRegistry.counter("chat.message.cache", "status", "fallback_clear").increment();
    }
}

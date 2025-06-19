package com.gary.application.chat;

import com.gary.application.common.MetricIncrement;
import com.gary.application.common.ResultStatus;
import com.gary.infrastructure.constants.RedisKeys;
import com.gary.domain.service.chat.ChatCacheService;
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
    private final MetricIncrement metricIncrement;


    @Override
    @LoggableAction("Cache Chat Message")
    @Timed("chat.cache.save.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "cacheMessageFallback")
    public void save(ChatMessageResponse message) {

        String key = RedisKeys.chatMessages(message.senderId(), message.receiverId());

        boolean isNewKey = !chatMessageRedisTemplate.hasKey(key);

        chatMessageRedisTemplate.opsForList().rightPush(key, message);

        if (isNewKey) {
            chatMessageRedisTemplate.expire(key, MESSAGE_EXPIRATION);
            log.info("New Redis key created: {}. Expiration set to {} hours", key, MESSAGE_EXPIRATION.toHours());
        }

        metricIncrement.incrementMetric("cache.chat.message.save","success");
    }

    @LoggableAction("Cache Chat Message Fallback")
    void cacheFallback(ChatMessageResponse response, Throwable t) {
        log.warn("Failed to cache message id={} after retries/circuit breaker: {}", response.id(), t.getMessage());
        metricIncrement.incrementMetric("cache.chat.message.save","fallback");
    }


    @Override
    @LoggableAction("Get Cached Messages")
    @Timed("chat.cache.getMessages.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getCachedMessagesFallback")
    public CachedMessagesResult getCachedMessages(UUID user1Id, UUID user2Id, int offset, int limit) {

        String key = RedisKeys.chatMessages(user1Id, user2Id);

        List<ChatMessageResponse> messages = chatMessageRedisTemplate.opsForList().range(key, offset, offset + limit - 1);

        ResultStatus status;

        if (messages == null || messages.isEmpty()) {
            log.info("No cached messages found for users [{} <-> {}] with offset={} and limit={}",
                    user1Id, user2Id, offset, limit);
            metricIncrement.incrementMetric("cache.chat.message.get","miss");
            status = ResultStatus.MISS;
        } else {
            log.info("Retrieved {} cached messages for users [{} <-> {}] with offset={} and limit={}",
                    messages.size(), user1Id, user2Id, offset, limit);
            metricIncrement.incrementMetric("cache.chat.message.get","hit");
            status = ResultStatus.HIT;
        }

        return CachedMessagesResult.builder()
                .messages(messages)
                .status(status)
                .build();
    }


    @LoggableAction("Get Cached Messages Fallback")
    CachedMessagesResult getCachedMessagesFallback(UUID user1Id, UUID user2Id, int offset, int limit, Throwable t) {
        log.warn("Failed to get cached messages for users [{} <-> {}]: {}", user1Id, user2Id, t.getMessage());
        metricIncrement.incrementMetric("cache.chat.message.get","fallback");


        CachedMessagesResult result = CachedMessagesResult.builder()
                .status(ResultStatus.FALLBACK)
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

        Long ttlSeconds = chatMessageRedisTemplate.getExpire(key);
        if (ttlSeconds != null && ttlSeconds > 0) {
            log.debug("TTL before deletion for key {}: {} seconds", key, ttlSeconds);
        }
        Boolean deleted = chatMessageRedisTemplate.delete(key);

        if (deleted) {
            log.info("Evicted chat cache for users [{} <-> {}] (key={})", user1Id, user2Id, key);
        } else {
            log.warn("Attempted to evict chat cache for users [{} <-> {}], but key was not found (key={})",
                    user1Id, user2Id, key);
        }

        metricIncrement.incrementMetric("cache.chat.message.clear","success");

    }

    @LoggableAction("Clear Cached Messages Fallback")
    void clearCachedMessagesFallback(UUID user1Id, UUID user2Id, Throwable t) {
        log.warn("Failed to clear cached messages for users [{} <-> {}]: {}", user1Id, user2Id, t.getMessage());
        metricIncrement.incrementMetric("cache.chat.message.clear","fallback");
    }
}

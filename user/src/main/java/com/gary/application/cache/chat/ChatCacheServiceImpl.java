package com.gary.application.cache.chat;

import com.gary.infrastructure.constants.RedisKeys;
import com.gary.domain.service.cache.ChatCacheService;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import com.gary.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatCacheServiceImpl implements ChatCacheService {

    private final RedisTemplate<String, ChatMessageResponse> chatMessageRedisTemplate;
    private static final Duration MESSAGE_EXPIRATION = Duration.ofHours(6);

    @Override
    @LoggableAction("Cache Chat Message")
    @Timed("chat.cache.message.duration")
    @RetryableOperation(maxAttempts = 2, retryOn = {RuntimeException.class})
    public void cacheMessage(ChatMessageResponse message) {
        String key = RedisKeys.chatMessages(message.senderId(), message.receiverId());
        boolean isNewKey = !chatMessageRedisTemplate.hasKey(key);

        chatMessageRedisTemplate.opsForList().rightPush(key, message);

        if (isNewKey) {
            chatMessageRedisTemplate.expire(key, MESSAGE_EXPIRATION);
            log.info("New Redis key created: {}. Expiration set to {} hours", key, MESSAGE_EXPIRATION.toHours());
        }

    }

    @Override
    @LoggableAction("Retrieve Cached Messages")
    @Timed("chat.cache.getMessages.duration")
    public List<ChatMessageResponse> getCachedMessages(Long user1Id, Long user2Id, int offset, int limit) {
        String key = RedisKeys.chatMessages(user1Id, user2Id);

        List<ChatMessageResponse> messages = chatMessageRedisTemplate.opsForList().range(key, offset, offset + limit - 1);

        if (messages == null || messages.isEmpty()) {
            log.info("No cached messages found for users [{} <-> {}] with offset={} and limit={}",
                    user1Id, user2Id, offset, limit);
        } else {
            log.info("Retrieved {} cached messages for users [{} <-> {}] with offset={} and limit={}",
                    messages.size(), user1Id, user2Id, offset, limit);
        }

        return messages;
    }

    @Override
    @LoggableAction("Clear Cached Messages")
    @Timed("chat.cache.clearMessages.duration")
    public void clearCachedMessages(Long user1Id, Long user2Id) {
        String key = RedisKeys.chatMessages(user1Id, user2Id);

        Boolean deleted = chatMessageRedisTemplate.delete(key);

        if (deleted) {
            log.info("Evicted chat cache for users [{} <-> {}] (key={})", user1Id, user2Id, key);
        } else {
            log.warn("Attempted to evict chat cache for users [{} <-> {}], but key was not found (key={})",
                    user1Id, user2Id, key);
        }
    }
}

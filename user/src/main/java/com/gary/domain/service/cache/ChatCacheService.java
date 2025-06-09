package com.gary.domain.service.cache;


import com.gary.config.RedisKeys;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatCacheService {

    private final RedisTemplate<String, ChatMessageResponse> chatMessageRedisTemplate;
    private static final Duration MESSAGE_EXPIRATION = Duration.ofHours(6);

    public void cacheMessage(ChatMessageResponse message) {
        String key = RedisKeys.chatMessages(message.senderId(), message.receiverId());
        chatMessageRedisTemplate.opsForList().rightPush(key, message);
        chatMessageRedisTemplate.expire(key, MESSAGE_EXPIRATION);
    }

    public List<ChatMessageResponse> getCachedMessages(Long user1Id, Long user2Id) {
        String key = RedisKeys.chatMessages(user1Id, user2Id);
        return chatMessageRedisTemplate.opsForList().range(key, 0, -1);
    }

    public void evictChatCache(Long user1Id, Long user2Id) {
        chatMessageRedisTemplate.delete(RedisKeys.chatMessages(user1Id, user2Id));
    }
}

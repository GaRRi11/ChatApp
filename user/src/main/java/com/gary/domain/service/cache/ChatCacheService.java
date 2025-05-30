package com.gary.domain.service.cache;

import com.gary.ChatApp.web.dto.chatMessage.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String MESSAGE_KEY_PREFIX = "chat:messages:";

    private String generateKey(Long senderId, Long receiverId) {
        Long first = Math.min(senderId, receiverId);
        Long second = Math.max(senderId, receiverId);
        return MESSAGE_KEY_PREFIX + first + ":" + second;
    }

    public void cacheMessage(ChatMessageResponse message) {
        String key = generateKey(message.senderId(), message.receiverId());
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, Duration.ofHours(6));
    }

    public List<Object> getCachedMessages(Long senderId, Long receiverId) {
        String key = generateKey(senderId, receiverId);
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public void evictChatCache(Long senderId, Long receiverId) {
        String key = generateKey(senderId, receiverId);
        redisTemplate.delete(key);
    }

}

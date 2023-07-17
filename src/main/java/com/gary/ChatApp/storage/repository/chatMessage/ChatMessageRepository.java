package com.gary.ChatApp.storage.repository.chatMessage;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepository {
    private static final String HASH_KEY = "message";
    private final RedisTemplate template;

    public ChatMessage save (ChatMessage chatMessage){
        template.opsForHash().put(HASH_KEY,chatMessage.getId(),chatMessage);
        return chatMessage;
    }

}

package com.gary.ChatApp.storage.repository.chatMessage;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisChatMessageRepository {
    private static final String HASH_KEY = "message";

    private final RedisTemplate template;
    @Autowired
    public RedisChatMessageRepository(@Qualifier("myRedisTemplate") RedisTemplate redisTemplate) {
        this.template = redisTemplate;
        // Constructor implementation
    }

    public ChatMessage save (ChatMessage chatMessage){
        template.opsForHash().put(HASH_KEY,chatMessage.getId(),chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> getAll(){
        return template.opsForHash().values(HASH_KEY);
    }

    public ChatMessage findById(Long id){
        return (ChatMessage) template.opsForHash().get(HASH_KEY,id);
    }

}

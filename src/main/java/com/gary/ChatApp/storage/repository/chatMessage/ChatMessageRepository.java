//package com.gary.ChatApp.storage.repository.chatMessage;
//
//import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.redis.core.PartialUpdate;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public class ChatMessageRepository {
//    private static final String HASH_KEY = "message";
//
//    private final RedisTemplate template;
//    @Autowired
//    public ChatMessageRepository(@Qualifier("myRedisTemplate") RedisTemplate redisTemplate) {
//        this.template = redisTemplate;
//        // Constructor implementation
//    }
//
//    public ChatMessage save (ChatMessage chatMessage){
//        template.opsForHash().put(HASH_KEY,chatMessage.getId(),chatMessage);
//        return chatMessage;
//    }
//
//    public List<ChatMessage> getAll(){
//        return template.opsForHash().values(HASH_KEY);
//    }
//
//}

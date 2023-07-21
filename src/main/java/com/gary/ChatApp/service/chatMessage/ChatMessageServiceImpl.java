package com.gary.ChatApp.service.chatMessage;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import com.gary.ChatApp.storage.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final RedisTemplate template;
    private static final String HASH_KEY = "message";
    private static final long EXPIRATION_TIME_SECONDS = 7200;

    @Autowired
    public ChatMessageServiceImpl(ChatMessageRepository chatMessageService, @Qualifier("myRedisTemplate") RedisTemplate redisTemplate) {
        this.chatMessageRepository = chatMessageService;
        this.template = redisTemplate;
    }

    public ChatMessage save (ChatMessage chatMessage){
        chatMessageRepository.save(chatMessage);
        String key = HASH_KEY + ":" + chatMessage.getId();
        template.opsForHash().put(key,"content",chatMessage.getContent());
        template.opsForHash().put(key,"sender",chatMessage.getSender());

        template.expire(key, EXPIRATION_TIME_SECONDS, TimeUnit.SECONDS);


        return chatMessage;
    }

    public ChatMessage findById(Long id){
        String key = "message:" + id;
        if (template.opsForHash().hasKey(key, "content") && template.opsForHash().hasKey(key, "sender")) {
            String content = (String) template.opsForHash().get(key, "content");
            String sender = (String) template.opsForHash().get(key, "sender");
            return save(new ChatMessage(id, content, sender));
        } else {
            Optional<ChatMessage> chatMessageOptional = chatMessageRepository.findById(id);
            return chatMessageOptional.orElse(null); //da rame logika rodis waishalos redisidan
        }
    }


    @Override
    public List<ChatMessage> getAll() {
        return chatMessageRepository.findAll();
    }


}

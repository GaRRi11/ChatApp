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
    private final RedisDB redisDB;

    @Autowired
    public ChatMessageServiceImpl(ChatMessageRepository chatMessageService,
                                  RedisDB redisDB) {
        this.chatMessageRepository = chatMessageService;
        this.redisDB = redisDB;
    }

    public ChatMessage save (ChatMessage chatMessage){
        chatMessageRepository.save(chatMessage);
        redisDB.cacheOneMessageInRedis(chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> getChatMessagesBetweenTwoUsers(Long senderId,Long receiverId){
        // Check Redis for cached messages
        List<ChatMessage> cachedMessages = redisDB.getChatMessagesBetweenTwoUsersFromRedis(senderId, receiverId);

        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            return cachedMessages; // Return messages from cache if found
        } else {
            // Fetch messages from primary database
            List<ChatMessage> messagesFromDb = chatMessageRepository.findBySenderAndReceiver(senderId, receiverId);

            // Cache messages in Redis
            redisDB.cacheMessagesInRedis(messagesFromDb);

            return messagesFromDb;
        }
    }


    @Override
    public List<ChatMessage> getAll() {
        return chatMessageRepository.findAll();
    }

    @Override
    public ChatMessage findById(Long id) {
        if (redisDB.findByIdFromRedis(id).isPresent()){
            return redisDB.findByIdFromRedis(id).get();
        }else {
            return chatMessageRepository.findById(id).get();
        }
    }


}

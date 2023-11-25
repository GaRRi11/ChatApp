package com.gary.ChatApp.service.chatMessage;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisDB {
    private static final String HASH_KEY = "message";
    private static final long EXPIRATION_TIME_SECONDS = 7200;
    private final RedisTemplate template;

    public RedisDB(@Qualifier("myRedisTemplate") RedisTemplate template) {
        this.template = template;
    }

    public List<ChatMessage> getChatMessagesBetweenTwoUsersFromRedis(Long senderId, Long receiverId) {
        String indexKey = "sender_receiver:" + senderId + "_" + receiverId;
        Set<Object> messageIds = template.opsForSet().members(indexKey);

        List<ChatMessage> messages = new ArrayList<>();
        if (messageIds != null) {
            for (Object messageId : messageIds) {
                String messageKey = "message:" + messageId.toString();
                String content = (String) template.opsForHash().get(messageKey, "content");
                Long sender = Long.parseLong((String) template.opsForHash().get(messageKey, "sender"));
                Long receiver = Long.parseLong((String) template.opsForHash().get(messageKey, "receiver"));
                messages.add(new ChatMessage(Long.parseLong(messageId.toString()), content, sender, receiver));
            }
        }
        return messages;
    }

    public Optional<ChatMessage> findByIdFromRedis(Long id){
        String key = "message:" + id;
        if (template.opsForHash().hasKey(key, "content") && template.opsForHash().hasKey(key, "sender")) {
            String content = (String) template.opsForHash().get(key, "content");
            Long sender = (Long) template.opsForHash().get(key, "sender");
            Long receiver = (Long) template.opsForHash().get(key, "receiver");
            return Optional.of(new ChatMessage(content, sender, receiver));
        }else {
            return Optional.empty();
        }
    }

    public void cacheOneMessageInRedis(ChatMessage chatMessage) {
            String key = HASH_KEY + ":" + chatMessage.getId();
            String content = chatMessage.getContent();
            Long sender = chatMessage.getSender();
            Long receiver = chatMessage.getReceiver();

            String messageKey = "message:" + chatMessage.getId();
            template.opsForHash().put(messageKey, "content", content);
            template.opsForHash().put(messageKey, "sender", sender);
            template.opsForHash().put(messageKey, "receiver", receiver);
            template.expire(messageKey, EXPIRATION_TIME_SECONDS, TimeUnit.SECONDS);

        String indexKey = "sender_receiver:" + sender + "_" + receiver;
        template.opsForSet().add(indexKey, chatMessage.getId().toString());

    }

    public void cacheMessagesInRedis(List<ChatMessage> messages) {
        for (ChatMessage message : messages) {
            String key = HASH_KEY + ":" + message.getId();
            String content = message.getContent();
            Long sender = message.getSender();
            Long receiver = message.getReceiver();

            String messageKey = "message:" + message.getId();
            template.opsForHash().put(messageKey, "content", content);
            template.opsForHash().put(messageKey, "sender", sender);
            template.opsForHash().put(messageKey, "receiver", receiver);
            template.expire(messageKey, EXPIRATION_TIME_SECONDS, TimeUnit.SECONDS);

            String indexKey = "sender_receiver:" + sender + "_" + receiver;
            template.opsForSet().add(indexKey, message.getId().toString());
        }
    }
}

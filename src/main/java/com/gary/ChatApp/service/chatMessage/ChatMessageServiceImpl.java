package com.gary.ChatApp.service.chatMessage;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
//import com.gary.ChatApp.storage.repository.chatMessage.ChatMessageRepository;
import com.gary.ChatApp.storage.repository.chatMessage.ChatMessageRepository;
import com.gary.ChatApp.storage.repository.chatMessage.RedisChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    private final RedisChatMessageRepository redisChatMessageRepository;

    public ChatMessage save (ChatMessage chatMessage){
        chatMessageRepository.save(chatMessage);
        redisChatMessageRepository.save(chatMessage);
       return chatMessage;
    }

    public ChatMessage findById(Long id){
        ChatMessage chatMessage = redisChatMessageRepository.findById(id);

        if (chatMessage != null){
            return chatMessage;
        }

        chatMessage = chatMessageRepository.findById(id).orElseThrow();
        redisChatMessageRepository.save(chatMessage);
        return chatMessage; //da rame logika rodis waishalos redisidan
    }

    @Override
    public List<ChatMessage> getAll() {
        return chatMessageRepository.findAll();
    }


}

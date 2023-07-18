package com.gary.ChatApp.service.chatMessage;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import com.gary.ChatApp.storage.repository.chatMessage.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage save (ChatMessage chatMessage){
       return chatMessageRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessage> getAll() {
       return chatMessageRepository.getAll();
    }


}

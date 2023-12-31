package com.gary.ChatApp.service.chatMessage;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    ChatMessage save(ChatMessage chatMessage);

    List<ChatMessage> getAll();

    ChatMessage findById(Long id);

     List<ChatMessage> getChatMessagesBetweenTwoUsers(Long senderId, Long receiverId);


}
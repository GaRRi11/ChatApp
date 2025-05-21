package com.gary.ChatApp.domain.service.chatMessage;

import com.gary.ChatApp.domain.model.chatmessage.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    ChatMessage sendMessage(Long senderId, Long receiverId, String content);
    List<ChatMessage> getChatHistory(Long user1Id, Long user2Id);
}

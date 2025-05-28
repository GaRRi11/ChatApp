package com.gary.ChatApp.domain.service.chatMessage;

import com.gary.ChatApp.domain.model.chatmessage.ChatMessage;
import com.gary.ChatApp.web.dto.ChatMessageDto;

import java.util.List;

public interface ChatMessageService {
    ChatMessage sendMessage(ChatMessageDto chatMessageDto);
    List<ChatMessage> getChatHistory(Long user1Id, Long user2Id);
}

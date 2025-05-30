package com.gary.ChatApp.domain.service.chat;

import com.gary.ChatApp.web.dto.chatMessage.ChatMessageRequest;
import com.gary.ChatApp.web.dto.chatMessage.ChatMessageResponse;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponse sendMessage(ChatMessageRequest request, Long userId);
    List<ChatMessageResponse> getChatHistory(Long user1Id, Long user2Id);
}

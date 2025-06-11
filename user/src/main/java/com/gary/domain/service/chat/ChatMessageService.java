package com.gary.domain.service.chat;

import com.gary.web.dto.chatMessage.ChatMessageRequest;
import com.gary.web.dto.chatMessage.ChatMessageResponse;

import java.util.List;

public interface ChatMessageService {
    ChatMessageResponse sendMessage(ChatMessageRequest request, Long userId);
    List<ChatMessageResponse> getChatHistory(Long user1Id, Long user2Id,int offset, int limit);
}

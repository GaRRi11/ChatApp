package com.gary.domain.service.chat;

import com.gary.web.dto.chatMessage.ChatMessageRequest;
import com.gary.web.dto.chatMessage.ChatMessageResponse;

import java.util.List;
import java.util.UUID;

public interface ChatMessageService {
    ChatMessageResponse sendMessage(ChatMessageRequest request, UUID userId);
    List<ChatMessageResponse> getChatHistory(UUID user1Id, UUID user2Id, int offset, int limit);
}

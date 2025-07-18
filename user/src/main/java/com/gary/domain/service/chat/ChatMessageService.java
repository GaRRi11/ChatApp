package com.gary.domain.service.chat;

import com.gary.web.dto.rest.chatMessage.ChatMessageRequest;
import com.gary.web.dto.rest.chatMessage.ChatMessageResponse;

import java.util.List;
import java.util.UUID;

public interface ChatMessageService {
    ChatMessageResponse sendMessage(ChatMessageRequest request, UUID userId);
    List<ChatMessageResponse> getChatHistory(UUID senderId, UUID receiverId, int offset, int limit);
}

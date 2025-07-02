package com.gary.domain.service.chat;

import com.gary.web.dto.rest.chatMessage.ChatMessageResponse;

import java.util.List;
import java.util.UUID;

public interface ChatCacheService {
    void save(ChatMessageResponse message);
    List<ChatMessageResponse> getCachedMessages(UUID user1Id, UUID user2Id, int offset, int limit);

    void saveAll(List<ChatMessageResponse> messages);
    void clearCachedMessages(UUID user1Id, UUID user2Id);
}

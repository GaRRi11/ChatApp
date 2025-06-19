package com.gary.domain.service.chat;

import com.gary.application.chat.CachedMessagesResult;
import com.gary.web.dto.chatMessage.ChatMessageResponse;

import java.util.UUID;

public interface ChatCacheService {
    void save(ChatMessageResponse message);

    CachedMessagesResult getCachedMessages(UUID user1Id, UUID user2Id, int offset, int limit);

    void clearCachedMessages(UUID user1Id, UUID user2Id);
}

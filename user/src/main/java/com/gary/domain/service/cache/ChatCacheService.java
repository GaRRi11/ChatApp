package com.gary.domain.service.cache;

import com.gary.web.dto.chatMessage.ChatMessageResponse;

import java.util.List;
import java.util.UUID;

public interface ChatCacheService {
     void cacheMessage(ChatMessageResponse message);
     List<ChatMessageResponse> getCachedMessages(UUID user1Id, UUID user2Id, int offset, int limit);
     void clearCachedMessages(UUID user1Id, UUID user2Id);
    }

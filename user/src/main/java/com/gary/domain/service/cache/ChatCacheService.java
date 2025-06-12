package com.gary.domain.service.cache;

import com.gary.web.dto.chatMessage.ChatMessageResponse;

import java.util.List;

public interface ChatCacheService {
     void cacheMessage(ChatMessageResponse message);
     List<ChatMessageResponse> getCachedMessages(Long user1Id, Long user2Id, int offset, int limit);
     void clearCachedMessages(Long user1Id, Long user2Id);
    }

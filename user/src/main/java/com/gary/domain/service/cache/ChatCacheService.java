package com.gary.domain.service.cache;

import com.gary.web.dto.chatMessage.ChatMessageResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ChatCacheService {
     void cacheMessage(ChatMessageResponse message);
     List<ChatMessageResponse> getCachedMessages(Long user1Id, Long user2Id, int offset, int limit);
     void evictChatCache(Long user1Id, Long user2Id);
    }

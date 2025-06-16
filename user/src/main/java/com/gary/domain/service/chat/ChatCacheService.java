package com.gary.domain.service.chat;

import com.gary.application.chat.CachedMessagesResult;
import com.gary.web.dto.chatMessage.ChatMessageResponse;

import java.util.UUID;

public interface ChatCacheService {
     void cacheMessage(ChatMessageResponse message);
     void cacheFallback(ChatMessageResponse response, Throwable t);
     CachedMessagesResult getCachedMessages(UUID user1Id, UUID user2Id, int offset, int limit);
     CachedMessagesResult getCachedMessagesFallback(UUID user1Id, UUID user2Id, int offset, int limit, Throwable t);
     void clearCachedMessages(UUID user1Id, UUID user2Id);
     void clearCachedMessagesFallback(UUID user1Id, UUID user2Id, Throwable t);
    }

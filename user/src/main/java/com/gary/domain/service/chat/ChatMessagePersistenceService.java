package com.gary.domain.service.chat;

import com.gary.application.chat.PersistedMessageResult;
import com.gary.domain.model.chatmessage.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ChatMessagePersistenceService {

    ChatMessage saveMessage(ChatMessage message);

    ChatMessage dbSaveFallback(ChatMessage message, Throwable t);

    PersistedMessageResult findChatBetweenUsers(UUID user1Id, UUID user2Id, int offset, int limit);

    PersistedMessageResult findChatFallback(UUID user1Id, UUID user2Id, int offset, int limit, Throwable t);


}


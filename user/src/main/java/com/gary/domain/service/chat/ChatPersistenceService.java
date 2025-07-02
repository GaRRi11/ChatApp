package com.gary.domain.service.chat;

import com.gary.domain.model.chatmessage.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ChatPersistenceService {

    ChatMessage saveMessage(ChatMessage message);


    List<ChatMessage> findChatBetweenUsers(UUID user1Id, UUID user2Id, int offset, int limit);


}


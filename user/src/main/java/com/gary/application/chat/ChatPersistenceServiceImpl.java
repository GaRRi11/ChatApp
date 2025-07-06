package com.gary.application.chat;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.repository.jpa.chatMessage.ChatMessageRepository;
import com.gary.domain.service.chat.ChatPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatPersistenceServiceImpl implements ChatPersistenceService {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    @LoggableAction("Save Chat Message")
    @Timed("chat.db.saveMessage.duration")
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }



    @Override
    @LoggableAction("Find Chat Between Users")
    @Timed("chat.db.findChat.duration")
    public List<ChatMessage> findChatBetweenUsers(UUID user1Id, UUID user2Id, int offset, int limit) {
        return chatMessageRepository.findChatBetweenUsers(user1Id, user2Id, offset, limit);
    }



}


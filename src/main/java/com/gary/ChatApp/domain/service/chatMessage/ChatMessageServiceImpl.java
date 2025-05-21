package com.gary.ChatApp.domain.service.chatMessage;

import com.gary.ChatApp.domain.model.chatmessage.ChatMessage;
import com.gary.ChatApp.repository.ChatMessageRepository;
import com.gary.ChatApp.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build();
        return chatMessageRepository.save(message);
    }

    @Override
    public List<ChatMessage> getChatHistory(Long user1Id, Long user2Id) {
        return chatMessageRepository.findChatBetweenUsers(user1Id, user2Id);
    }
}

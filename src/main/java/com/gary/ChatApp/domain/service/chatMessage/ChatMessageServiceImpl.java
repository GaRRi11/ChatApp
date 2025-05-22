package com.gary.ChatApp.domain.service.chatMessage;

import com.gary.ChatApp.domain.model.chatmessage.ChatMessage;
import com.gary.ChatApp.domain.repository.ChatMessageRepository;
import com.gary.ChatApp.domain.service.chatCacheService.ChatCacheService;
import com.gary.ChatApp.domain.service.rateLimiterService.RateLimiterService;
import com.gary.ChatApp.exceptions.TooManyRequestsException;
import com.gary.ChatApp.web.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final RateLimiterService rateLimiterService;
    private final ChatCacheService chatCacheService; // 👈 Add this

    @Override
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        if (!rateLimiterService.isAllowedToSend(senderId)) {
            throw new TooManyRequestsException("You're sending messages too quickly. Please wait.");
        }

        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Convert to DTO for caching (or use mapper)
        ChatMessageDto dto = new ChatMessageDto();
        dto.setSenderId(senderId);
        dto.setReceiverId(receiverId);
        dto.setContent(content);
        dto.setTimestamp(savedMessage.getSentAt());

        chatCacheService.cacheMessage(dto); // 👈 Cache it in Redis too

        return savedMessage;
    }

    @Override
    public List<ChatMessage> getChatHistory(Long user1Id, Long user2Id) {
        return chatMessageRepository.findChatBetweenUsers(user1Id, user2Id);
    }
}

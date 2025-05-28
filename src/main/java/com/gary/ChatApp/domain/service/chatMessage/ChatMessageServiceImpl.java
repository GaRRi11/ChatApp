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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final RateLimiterService rateLimiterService;
    private final ChatCacheService chatCacheService;

    @Override
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        if (!rateLimiterService.isAllowedToSend(senderId)) {
            throw new TooManyRequestsException("You're sending messages too quickly. Please wait.");
        }

        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        chatCacheService.cacheMessage(ChatMessageDto.fromEntity(savedMessage));

        return savedMessage;
    }

    @Override
    public List<ChatMessage> getChatHistory(Long user1Id, Long user2Id) {
        return chatMessageRepository.findChatBetweenUsers(user1Id, user2Id);
    }

    // New method returning DTOs and using cache
    public List<ChatMessageDto> getChatHistoryDto(Long user1Id, Long user2Id) {
        List<Object> cached = chatCacheService.getCachedMessages(user1Id, user2Id);

        if (cached != null && !cached.isEmpty()) {
            return cached.stream()
                    .map(obj -> (ChatMessageDto) obj)
                    .collect(Collectors.toList());
        }

        // Cache miss fallback to DB
        List<ChatMessage> messages = chatMessageRepository.findChatBetweenUsers(user1Id, user2Id);

        // Cache all fetched messages
        messages.forEach(msg -> chatCacheService.cacheMessage(ChatMessageDto.fromEntity(msg)));

        return messages.stream()
                .map(ChatMessageDto::fromEntity)
                .collect(Collectors.toList());
    }
}

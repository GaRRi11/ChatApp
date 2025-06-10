package com.gary.domain.service.chat;

import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.repository.ChatMessageRepository;
import com.gary.domain.service.cache.ChatCacheService;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.exceptions.TooManyRequestsException;
import com.gary.web.dto.chatMessage.ChatMessageRequest;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final RateLimiterService rateLimiterService;
    private final ChatCacheService chatCacheService;


    @Override
    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long senderId) {
        if (!rateLimiterService.isAllowedToSend(senderId)) {
            throw new TooManyRequestsException("You're sending messages too quickly. Please wait.");
        }

        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(request.receiverId())
                .content(request.content().trim())
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageResponse response = ChatMessageResponse.fromEntity(saved);

        chatCacheService.cacheMessage(response);
        return response;
    }

    @Override
    public List<ChatMessageResponse> getChatHistory(Long user1Id, Long user2Id, int offset, int limit) {
        List<ChatMessageResponse> cachedMessages = chatCacheService.getCachedMessages(user1Id, user2Id, offset, limit);

        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            log.info("Cache HIT for chat between {} and {}", user1Id, user2Id);
            return cachedMessages;
        }

        log.info("Cache MISS for chat between {} and {}", user1Id, user2Id);
        List<ChatMessage> messages = chatMessageRepository.findChatBetweenUsers(user1Id, user2Id, offset, limit);
        List<ChatMessageResponse> responses = messages.stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());

        responses.forEach(chatCacheService::cacheMessage);

        return responses;
    }

}

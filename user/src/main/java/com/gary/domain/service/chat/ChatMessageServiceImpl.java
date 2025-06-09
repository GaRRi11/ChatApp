package com.gary.domain.service.chat;

import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.repository.ChatMessageRepository;
import com.gary.domain.service.cache.ChatCacheService;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.exceptions.TooManyRequestsException;
import com.gary.web.dto.chatMessage.ChatMessageRequest;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
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
    public List<ChatMessageResponse> getChatHistory(Long user1Id, Long user2Id) {
        List<ChatMessageResponse> cachedMessages = chatCacheService.getCachedMessages(user1Id, user2Id);

        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            return cachedMessages;
        }

        List<ChatMessage> messages = chatMessageRepository.findChatBetweenUsers(user1Id, user2Id);
        List<ChatMessageResponse> responses = messages.stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());

        responses.forEach(chatCacheService::cacheMessage);

        return responses;
    }

}

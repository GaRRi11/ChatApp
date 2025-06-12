package com.gary.application.chat;

import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.repository.chatMessage.ChatMessageRepository;
import com.gary.domain.service.cache.ChatCacheService;
import com.gary.domain.service.chat.ChatMessageService;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.web.exception.TooManyRequestsException;
import com.gary.web.dto.chatMessage.ChatMessageRequest;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import com.gary.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
    @LoggableAction("Send Chat Message")
    @Timed("chat.message.send.duration")
    @RetryableOperation(maxAttempts = 3, retryOn = {RuntimeException.class})
    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long senderId) {

        if (!rateLimiterService.isAllowedToSend(senderId)) {
            log.warn("Rate limit exceeded for senderId={}", senderId);
            throw new TooManyRequestsException("You're sending messages too quickly. Please wait.");
        }

        String contentPreview = request.content().trim();
        log.info("Processing message from senderId={} to receiverId={} (length={} chars)",
                senderId, request.receiverId(), contentPreview.length());

        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(request.receiverId())
                .content(request.content().trim())
                .timestamp(LocalDateTime.now())
                .build();


        //try catch
        ChatMessage savedMessage = chatMessageRepository.save(message);

        log.info("Message saved to DB: id={}, senderId={}, receiverId={}",
                savedMessage.getId(), senderId, request.receiverId());

        ChatMessageResponse response = ChatMessageResponse.fromEntity(savedMessage);

        try {

            chatCacheService.cacheMessage(response);

        } catch (DataAccessException e) {

            log.warn("Message persisted to DB, but caching threw exception: {}", e.getMessage());

        }

        return response;
    }

    @Override
    @LoggableAction("Get Chat History")
    @Timed("chat.message.getHistory.duration")
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

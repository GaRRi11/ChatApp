    package com.gary.application.chat;

    import com.gary.application.rateLimiter.RateLimiterStatus;
    import com.gary.domain.model.chatmessage.ChatMessage;
    import com.gary.domain.service.chat.ChatCacheService;
    import com.gary.domain.service.chat.ChatMessageService;
    import com.gary.domain.service.rateLimiter.RateLimiterService;
    import com.gary.web.exception.MessagePersistenceException;
    import com.gary.web.exception.RateLimiterServiceUnavailableException;
    import com.gary.web.exception.TooManyRequestsException;
    import com.gary.web.dto.chatMessage.ChatMessageRequest;
    import com.gary.web.dto.chatMessage.ChatMessageResponse;
    import com.gary.annotations.*;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;

    import org.springframework.scheduling.annotation.Async;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.UUID;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class ChatMessageServiceImpl implements ChatMessageService {

        private final RateLimiterService rateLimiterService;
        private final ChatCacheService chatCacheService;
        private final ChatPersistenceServiceImpl chatPersistenceService;

        @Override
        @LoggableAction("Send Chat Message")
        @Timed("chat.message.send.duration")
        public ChatMessageResponse sendMessage(ChatMessageRequest request, UUID senderId) {

            RateLimiterStatus status = rateLimiterService.isAllowedToSend(senderId);

            switch (status) {
                case ALLOWED:

                    ChatMessage message = buildMessageEntity(request, senderId);
                    ChatMessage savedMessage = chatPersistenceService.saveMessage(message);

                    if (savedMessage != null) {
                        log.error("Failed to save message for sender {}", senderId);
                        throw new MessagePersistenceException("Message service is temporarily unavailable. Please try again later.");
                    }

                    ChatMessageResponse response = ChatMessageResponse.fromEntity(savedMessage);
                    cacheMessageAsync(response);
                    return response;

                case BLOCKED:

                    log.warn("User {} is blocked by rate limiter due to sending messages too quickly.", senderId);
                    throw new TooManyRequestsException("You're sending messages too quickly. Please wait.");

                case UNAVAILABLE:
                default:

                    log.error("Rate limiter service unavailable for user {}. Throwing exception.", senderId);
                    throw new RateLimiterServiceUnavailableException("Message service is temporarily unavailable. Please try again later.");
            }
        }




        @Override
        @LoggableAction("Get Chat History")
        @Timed("chat.message.getHistory.duration")
        public List<ChatMessageResponse> getChatHistory(UUID user1Id, UUID user2Id, int offset, int limit) {
            CachedMessagesResult cacheResult = chatCacheService.getCachedMessages(user1Id, user2Id, offset, limit);

            if (cacheResult.status() == ResultStatus.HIT) {
                return cacheResult.messages();
            }

            PersistedMessageResult persistedResult = chatPersistenceService.findChatBetweenUsers(user1Id, user2Id, offset, limit);

            if (persistedResult.status() == ResultStatus.FALLBACK) {
                log.error("Database fallback while fetching messages for users [{} <-> {}]", user1Id, user2Id);
                throw new MessagePersistenceException("Message service is temporarily unavailable. Please try again later.");
            }

            List<ChatMessageResponse> responses = persistedResult.messages().stream()
                    .map(ChatMessageResponse::fromEntity)
                    .toList();

            if (cacheResult.status() == ResultStatus.FALLBACK) {
                log.warn("Cache fallback detected, skipping cache update for users [{} <-> {}]", user1Id, user2Id);
                return responses;
            }

            if (cacheResult.status() == ResultStatus.MISS && !responses.isEmpty()) {
                log.info("Caching {} messages for users [{} <-> {}]", responses.size(), user1Id, user2Id);
                responses.forEach(chatCacheService::cacheMessage);
            }

            return responses;
        }


        @Async("taskExecutor")
        public void cacheMessageAsync(ChatMessageResponse response) {
            chatCacheService.cacheMessage(response);
        }

        private ChatMessage buildMessageEntity(ChatMessageRequest request, UUID senderId) {
            return ChatMessage.builder()
                    .senderId(senderId)
                    .receiverId(request.receiverId())
                    .content(request.content().trim())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

    }





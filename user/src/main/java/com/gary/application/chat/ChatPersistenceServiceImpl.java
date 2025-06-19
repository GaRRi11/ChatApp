package com.gary.application.chat;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.common.MetricIncrement;
import com.gary.application.common.ResultStatus;
import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.repository.chatMessage.ChatMessageRepository;
import com.gary.domain.service.chat.ChatMessagePersistenceService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatPersistenceServiceImpl implements ChatMessagePersistenceService {

    private final ChatMessageRepository chatMessageRepository;
    private final MetricIncrement metricIncrement;

    @Override
    @LoggableAction("Save Chat Message")
    @Timed("chat.db.saveMessage.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "dbSaveFallback")
    public ChatMessage saveMessage(ChatMessage message) {

        ChatMessage saved = chatMessageRepository.save(message);
        log.info("Message saved to DB: {}", saved.getId());
        metricIncrement.incrementMetric("db.chat.message.save","success");
        return saved;
    }

    @LoggableAction("Save Chat Message Fallback")
    ChatMessage dbSaveFallback(ChatMessage message, Throwable t) {
        log.error("DB save operation failed permanently: {}", t.toString());
        metricIncrement.incrementMetric("db.chat.message.save","fallback");
        return null;
    }


    @Override
    @LoggableAction("Find Chat Between Users")
    @Timed("chat.db.findChat.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "findChatFallback")
    public PersistedMessageResult findChatBetweenUsers(UUID user1Id, UUID user2Id, int offset, int limit) {
        List<ChatMessage> messages = chatMessageRepository.findChatBetweenUsers(user1Id, user2Id, offset, limit);
        metricIncrement.incrementMetric("db.chat.message.find","success");

        return PersistedMessageResult
                .builder()
                .messages(messages)
                .status(ResultStatus.HIT)
                .build();
    }

    @LoggableAction("Find Chat Between Users Fallback")
    PersistedMessageResult findChatFallback(UUID user1Id, UUID user2Id, int offset, int limit, Throwable t) {
        log.error("DB find operation failed permanently: {}", t.toString());
        metricIncrement.incrementMetric("db.chat.message.find","fallback");

        return PersistedMessageResult
                .builder()
                .status(ResultStatus.FALLBACK)
                .build();
    }


}


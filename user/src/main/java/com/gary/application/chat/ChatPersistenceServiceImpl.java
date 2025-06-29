package com.gary.application.chat;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.common.metric.MetricIncrement;
import com.gary.common.ResultStatus;
import com.gary.common.time.TimeFormat;
import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.repository.jpa.chatMessage.ChatMessageRepository;
import com.gary.domain.service.chat.ChatPersistenceService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatPersistenceServiceImpl implements ChatPersistenceService {

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

    ChatMessage dbSaveFallback(ChatMessage message, Throwable t) {
        log.error("Timestamp='{}' DB save failed permanently. Message senderId={}, receiverId={}, content='{}'. Cause: {}",
                TimeFormat.nowTimestamp(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent().substring(0, Math.min(100, message.getContent().length())),  // trim for safety
                t.toString());
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

    PersistedMessageResult findChatFallback(UUID user1Id, UUID user2Id, int offset, int limit, Throwable t) {

        log.error("Timestamp='{}' DB find operation failed permanently for users [{} <-> {}] with offset={} and limit={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                user1Id,
                user2Id,
                offset,
                limit,
                t.toString());

        metricIncrement.incrementMetric("db.chat.message.find","fallback");

        return PersistedMessageResult
                .builder()
                .status(ResultStatus.FALLBACK)
                .build();
    }


}


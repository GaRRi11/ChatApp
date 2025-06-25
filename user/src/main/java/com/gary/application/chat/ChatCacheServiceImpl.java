package com.gary.application.chat;

import com.gary.common.metric.MetricIncrement;
import com.gary.common.ResultStatus;
import com.gary.common.time.TimeFormat;
import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.domain.repository.cache.chatMessage.ChatMessageCacheRepository;
import com.gary.domain.service.chat.ChatCacheService;
import com.gary.web.dto.cache.chatMessage.ChatMessageCacheDto;
import com.gary.web.dto.rest.chatMessage.ChatMessageResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatCacheServiceImpl implements ChatCacheService {

    private final MetricIncrement metricIncrement;
    private final ChatMessageCacheRepository  chatMessageCacheRepository;

    @Override
    @LoggableAction("Cache Chat Message")
    @Timed("chat.cache.save.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "saveFallback")
    public void save(ChatMessageResponse message) {

        ChatMessageCacheDto chatMessageCacheDto = ChatMessageCacheDto.builder()
                .id(message.id())
                .senderId(message.senderId())
                .receiverId(message.receiverId())
                .content(message.content())
                .timestamp(message.timestamp())
                .build();

        chatMessageCacheRepository.save(chatMessageCacheDto);

        log.info("Timestamp='{}' Chat message cached: {}", TimeFormat.nowTimestamp(), message.id());

        metricIncrement.incrementMetric("cache.chat.message.save", "success");
    }

    void saveFallback(ChatMessageResponse response, Throwable t) {

        log.warn("Timestamp='{}' Cache save failed permanently. Message id={}, senderId={}, receiverId={}, content='{}'. Cause: {}",
                TimeFormat.nowTimestamp(),
                response.id(),
                response.senderId(),
                response.receiverId(),
                response.content().substring(0, Math.min(100, response.content().length())),
                t.toString());

        metricIncrement.incrementMetric("cache.chat.message.save", "fallback");
    }


    @Override
    @LoggableAction("Get Cached Messages")
    @Timed("chat.cache.getMessages.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getCachedMessagesFallback")
    public CachedMessagesResult getCachedMessages(UUID user1Id, UUID user2Id, int offset, int limit) {

        List<ChatMessageCacheDto> rawMessages = chatMessageCacheRepository
                .findBySenderIdAndReceiverId(user1Id, user2Id);

        List<ChatMessageResponse> messages = rawMessages.stream()
                .sorted(Comparator.comparing(ChatMessageCacheDto::getTimestamp))
                .skip(offset)
                .limit(limit)
                .map(this::mapToResponse)
                .toList();

        ResultStatus status;

        if (messages.isEmpty()) {
            log.info("Timestamp='{}' No cached messages found for users [{} <-> {}] with offset={} and limit={}",
                    TimeFormat.nowTimestamp(), user1Id, user2Id, offset, limit);
            metricIncrement.incrementMetric("cache.chat.message.get", "miss");
            status = ResultStatus.MISS;
        } else {
            log.info("Timestamp='{}' {} cached messages for users [{} <-> {}] with offset={} and limit={}",
                    TimeFormat.nowTimestamp(), messages.size(), user1Id, user2Id, offset, limit);
            metricIncrement.incrementMetric("cache.chat.message.get", "hit");
            status = ResultStatus.HIT;
        }

        return CachedMessagesResult.builder()
                .messages(messages)
                .status(status)
                .build();
    }



    CachedMessagesResult getCachedMessagesFallback(UUID user1Id, UUID user2Id, int offset, int limit, Throwable t) {

        log.warn("Timestamp='{}' Failed to get cached messages permanently. Users [{} <-> {}], offset={}, limit={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                user1Id,
                user2Id,
                offset,
                limit,
                t.toString());

        metricIncrement.incrementMetric("cache.chat.message.get", "fallback");


        return CachedMessagesResult.builder()
                .status(ResultStatus.FALLBACK)
                .build();
    }

    @Override
    @LoggableAction("Clear Cached Messages")
    @Timed("chat.cache.clearMessages.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "clearCachedMessagesFallback")
    public void clearCachedMessages(UUID user1Id, UUID user2Id) {

        List<ChatMessageCacheDto> messages = chatMessageCacheRepository.findBySenderIdAndReceiverId(user1Id, user2Id);
        chatMessageCacheRepository.deleteAll(messages);

        log.info("Timestamp='{}' Cleared cached messages for users [{} -> {}]",
                TimeFormat.nowTimestamp(), user1Id, user2Id);

        metricIncrement.incrementMetric("cache.chat.message.clear", "success");

    }

    void clearCachedMessagesFallback(UUID user1Id, UUID user2Id, Throwable t) {

        log.warn("Timestamp='{}' Failed to clear cached messages permanently. Users [{} <-> {}]. Cause: {}",
                TimeFormat.nowTimestamp(),
                user1Id,
                user2Id,
                t.toString());

        metricIncrement.incrementMetric("cache.chat.message.clear", "fallback");
    }

    private ChatMessageResponse mapToResponse(ChatMessageCacheDto dto) {
        return ChatMessageResponse.builder()
                .id(dto.getId())
                .senderId(dto.getSenderId())
                .receiverId(dto.getReceiverId())
                .content(dto.getContent())
                .timestamp(dto.getTimestamp())
                .build();
    }

}

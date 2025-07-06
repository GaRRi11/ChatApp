package com.gary.application.chat;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.domain.repository.cache.chatMessage.ChatMessageCacheRepository;
import com.gary.domain.service.chat.ChatCacheService;
import com.gary.web.dto.cache.chatMessage.ChatMessageCacheDto;
import com.gary.web.dto.rest.chatMessage.ChatMessageResponse;
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

    private final ChatMessageCacheRepository chatMessageCacheRepository;

    @Override
    @LoggableAction("Cache Chat Message")
    @Timed("chat.cache.save.duration")
    public void save(ChatMessageResponse message) {

        ChatMessageCacheDto chatMessageCacheDto = ChatMessageCacheDto.builder()
                .id(message.id())
                .senderId(message.senderId())
                .receiverId(message.receiverId())
                .content(message.content())
                .timestamp(message.timestamp())
                .build();

        chatMessageCacheRepository.save(chatMessageCacheDto);
    }


    @Override
    @LoggableAction("Cache Multiple Chat Messages")
    @Timed("chat.cache.saveAll.duration")
    public void saveAll(List<ChatMessageResponse> messages) {
        List<ChatMessageCacheDto> cacheDtos = messages.stream()
                .map(message -> ChatMessageCacheDto.builder()
                        .id(message.id())
                        .senderId(message.senderId())
                        .receiverId(message.receiverId())
                        .content(message.content())
                        .timestamp(message.timestamp())
                        .build())
                .toList();

        chatMessageCacheRepository.saveAll(cacheDtos);
    }


    @Override
    @LoggableAction("Get Cached Messages")
    @Timed("chat.cache.getMessages.duration")
    public List<ChatMessageResponse> getCachedMessages(UUID user1Id, UUID user2Id, int offset, int limit) {

        List<ChatMessageCacheDto> rawMessages = chatMessageCacheRepository
                .findBySenderIdAndReceiverId(user1Id, user2Id);

        List<ChatMessageResponse> messages = rawMessages.stream()
                .sorted(Comparator.comparing(ChatMessageCacheDto::getTimestamp)
                        .thenComparing(ChatMessageCacheDto::getId))
                .skip(offset)
                .limit(limit)
                .map(this::mapToResponse)
                .toList();


        return messages;
    }


    @Override
    @LoggableAction("Clear Cached Messages")
    @Timed("chat.cache.clearMessages.duration")
    public void clearCachedMessages(UUID user1Id, UUID user2Id) {

        List<ChatMessageCacheDto> messages = chatMessageCacheRepository.findBySenderIdAndReceiverId(user1Id, user2Id);
        chatMessageCacheRepository.deleteAll(messages);

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

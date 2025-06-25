package com.gary.domain.repository.chatMessage.cache;

import com.gary.web.dto.chatMessage.cache.ChatMessageCacheDto;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageCacheRepository extends RedisDocumentRepository<ChatMessageCacheDto, UUID> {
    List<ChatMessageCacheDto> findBySenderIdAndReceiverId(UUID senderId, UUID receiverId);
}

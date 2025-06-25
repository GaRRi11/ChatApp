package com.gary.domain.repository.cache.chatMessage;

import com.gary.web.dto.cache.chatMessage.ChatMessageCacheDto;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageCacheRepository extends RedisDocumentRepository<ChatMessageCacheDto, UUID> {

    List<ChatMessageCacheDto> findBySenderIdAndReceiverId(UUID senderId, UUID receiverId);

}

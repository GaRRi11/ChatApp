package com.gary.application.cache.chat;

import com.gary.web.dto.chatMessage.ChatMessageResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record CachedMessagesResult(List<ChatMessageResponse> messages, boolean fallbackUsed) {
}


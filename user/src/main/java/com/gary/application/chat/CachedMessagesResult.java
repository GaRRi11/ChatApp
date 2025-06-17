package com.gary.application.chat;

import com.gary.application.common.ResultStatus;
import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record CachedMessagesResult(
        List<ChatMessageResponse> messages,
        ResultStatus status
) {}


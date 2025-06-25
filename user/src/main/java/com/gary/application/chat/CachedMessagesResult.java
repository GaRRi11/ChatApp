package com.gary.application.chat;

import com.gary.common.ResultStatus;
import com.gary.web.dto.chatMessage.rest.ChatMessageResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record CachedMessagesResult(
        List<ChatMessageResponse> messages,
        ResultStatus status
) {}


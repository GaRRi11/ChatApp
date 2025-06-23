package com.gary.application.chat;

import com.gary.common.ResultStatus;
import com.gary.domain.model.chatmessage.ChatMessage;
import lombok.Builder;

import java.util.List;

@Builder
public record PersistedMessageResult(List<ChatMessage> messages, ResultStatus status) {
}

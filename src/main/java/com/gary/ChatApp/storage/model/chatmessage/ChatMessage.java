package com.gary.ChatApp.storage.model.chatmessage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

import static jakarta.persistence.GenerationType.AUTO;

@Data
@NoArgsConstructor
@RedisHash("message")
@Builder
public class ChatMessage implements Serializable {
    private Long id;
    private String content;
    private String sender; //maybe jpa ti davukavshiro users

    public ChatMessage(Long id,String content, String sender) {
        this.id = id;
        this.content = content;
        this.sender = sender;
    }
//private MessageType messageType;
}

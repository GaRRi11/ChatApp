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
@Builder
@RedisHash("message")
@Entity(name = "message")
@Table(name = "message")
public class ChatMessage implements Serializable {

    @Id
    @SequenceGenerator(
            name = "message_sequence",
            sequenceName = "message_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = AUTO,
            generator = "message_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column(

            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    @Column(

            name = "sender",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String sender; //maybe jpa ti davukavshiro users

    public ChatMessage(Long id,String content, String sender) {
        this.id = id;
        this.content = content;
        this.sender = sender;
    }
//private MessageType messageType;
}

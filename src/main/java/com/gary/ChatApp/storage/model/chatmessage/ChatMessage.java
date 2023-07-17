package com.gary.ChatApp.storage.model.chatmessage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import static jakarta.persistence.GenerationType.AUTO;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@RedisHash("message")
@Entity(name = "message")
@Table(name = "message")
public class ChatMessage {

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

    //private MessageType messageType;
}

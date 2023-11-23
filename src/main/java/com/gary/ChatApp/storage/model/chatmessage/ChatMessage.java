package com.gary.ChatApp.storage.model.chatmessage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static jakarta.persistence.GenerationType.AUTO;


@Entity(name = "ChatMessage")
@Table(name = "chat_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @SequenceGenerator(
            name = "chat_message_sequence",
            sequenceName = "chat_message_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = AUTO,
            generator = "chat_message_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;


    @Column(
            name = "content",
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    @Column(
            name = "sender",
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String sender; //maybe jpa ti davukavshiro users da usercontextit

    @Column(
            name = "receiver",
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String receiver;

    public ChatMessage(String content, String sender,String receiver) {
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
    }
    //private MessageType messageType;
}

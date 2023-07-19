package com.gary.ChatApp.storage.repository.chatMessage;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface MessageRepository extends JpaRepository<ChatMessage,Long> {
}

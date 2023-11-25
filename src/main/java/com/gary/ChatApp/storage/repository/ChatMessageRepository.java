package com.gary.ChatApp.storage.repository;

import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    List<ChatMessage> findBySenderAndReceiver(Long senderId, Long receiverId);

}

package com.gary.domain.repository;

import com.gary.domain.model.chatmessage.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT c FROM ChatMessage c WHERE " +
            "(c.senderId = :user1 AND c.receiverId = :user2) OR " +
            "(c.senderId = :user2 AND c.receiverId = :user1) " +
            "ORDER BY c.timestamp ASC")
    List<ChatMessage> findChatBetweenUsers(Long user1, Long user2);
}

package com.gary.domain.repository.chatMessage;

import com.gary.domain.model.chatmessage.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query(value = "SELECT * FROM chat_message WHERE " +
            "(sender_id = :user1 AND receiver_id = :user2) OR " +
            "(sender_id = :user2 AND receiver_id = :user1) " +
            "ORDER BY timestamp ASC " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<ChatMessage> findChatBetweenUsers(@Param("user1") UUID user1,
                                           @Param("user2") UUID user2,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

}

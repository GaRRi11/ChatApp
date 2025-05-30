package com.gary.domain.repository;

import com.gary.ChatApp.domain.model.chatmessage.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderIdAndReceiverIdOrderBySentAtAsc(Long senderId, Long receiverId);

    List<ChatMessage> findByReceiverIdAndSenderIdOrderBySentAtAsc(Long receiverId, Long senderId);

    // Optional: retrieve all between two users regardless of direction
    default List<ChatMessage> findChatBetweenUsers(Long user1, Long user2) {
        List<ChatMessage> messagesFrom1To2 = findBySenderIdAndReceiverIdOrderBySentAtAsc(user1, user2);
        List<ChatMessage> messagesFrom2To1 = findByReceiverIdAndSenderIdOrderBySentAtAsc(user1, user2);
        messagesFrom1To2.addAll(messagesFrom2To1);
        messagesFrom1To2.sort((a, b) -> a.getSentAt().compareTo(b.getSentAt()));
        return messagesFrom1To2;
    }
}

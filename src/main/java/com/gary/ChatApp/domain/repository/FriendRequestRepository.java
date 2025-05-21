package com.gary.ChatApp.domain.repository;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest,Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

}

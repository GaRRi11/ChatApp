package com.gary.ChatApp.storage.repository;

import com.gary.ChatApp.storage.model.friendrequest.FriendRequest;
import com.gary.ChatApp.storage.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest,Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

}

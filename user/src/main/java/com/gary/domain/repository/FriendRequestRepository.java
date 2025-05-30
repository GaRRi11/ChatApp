package com.gary.domain.repository;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, RequestStatus status);

    List<FriendRequest> findBySenderId(Long senderId);

    List<FriendRequest> findByReceiverId(Long receiverId);
}

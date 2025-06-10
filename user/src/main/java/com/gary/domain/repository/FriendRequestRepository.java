package com.gary.domain.repository;

import com.gary.domain.model.friendrequest.FriendRequest;
import com.gary.domain.model.friendrequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findBySenderIdAndStatus(Long senderId, RequestStatus status);
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, RequestStatus status);

}

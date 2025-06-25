package com.gary.domain.repository.jpa.friendRequest;

import com.gary.domain.model.friendrequest.FriendRequest;
import com.gary.domain.model.friendrequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

    List<FriendRequest> findBySenderIdAndStatus(UUID senderId, RequestStatus status);
    List<FriendRequest> findByReceiverIdAndStatus(UUID receiverId, RequestStatus status);
    boolean existsBySenderIdAndReceiverIdAndStatusIn(UUID senderId, UUID receiverId, List<RequestStatus> statuses);
}

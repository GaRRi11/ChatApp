package com.gary.domain.repository.friendRequest;

import com.gary.domain.model.friendrequest.FriendRequest;
import com.gary.domain.model.friendrequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findBySenderIdAndStatus(Long senderId, RequestStatus status);
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, RequestStatus status);
    boolean existsBySenderIdAndReceiverIdAndStatusIn(Long senderId, Long receiverId, List<RequestStatus> statuses);
}

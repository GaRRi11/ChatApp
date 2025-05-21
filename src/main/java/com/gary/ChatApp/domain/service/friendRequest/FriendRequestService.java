package com.gary.ChatApp.domain.service.friendRequest;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;

import java.util.List;

public interface FriendRequestService {
    FriendRequest sendRequest(Long senderId, Long receiverId);
    void respondToRequest(Long requestId, boolean accepted);
    List<FriendRequest> getPendingRequests(Long userId);
}

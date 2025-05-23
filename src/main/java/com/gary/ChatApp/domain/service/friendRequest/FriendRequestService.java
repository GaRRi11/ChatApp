package com.gary.ChatApp.domain.service.friendRequest;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.web.dto.FriendRequestDto;

import java.util.List;

public interface FriendRequestService {
    FriendRequestDto sendRequest(Long senderId, Long receiverId);
    void respondToRequest(Long requestId, boolean accepted);
    List<FriendRequestDto> getPendingRequests(Long userId);
}

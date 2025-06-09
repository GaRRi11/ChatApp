package com.gary.domain.service.friendRequest;

import com.gary.web.dto.respondToFriendDto.RespondToFriendDto;
import com.gary.web.dto.friendRequest.FriendRequestResponse;

import java.util.List;

public interface FriendRequestService {
    FriendRequestResponse sendRequest(Long senderId, Long receiverId);
    void respondToRequest(RespondToFriendDto responseDto, Long userId);
    List<FriendRequestResponse> getPendingRequests(Long userId);
}

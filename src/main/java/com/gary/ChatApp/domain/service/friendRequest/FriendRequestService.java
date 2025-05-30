package com.gary.ChatApp.domain.service.friendRequest;

import com.gary.ChatApp.web.dto.FriendRequestCreate;
import com.gary.ChatApp.web.dto.RespondToFriendDto;
import com.gary.ChatApp.web.dto.friendRequest.FriendRequestResponse;

import java.util.List;

public interface FriendRequestService {
    FriendRequestResponse sendRequest(FriendRequestCreate request, Long senderId);
    void respondToRequest(RespondToFriendDto responseDto, Long userId);
    List<FriendRequestResponse> getPendingRequests(Long userId);
}

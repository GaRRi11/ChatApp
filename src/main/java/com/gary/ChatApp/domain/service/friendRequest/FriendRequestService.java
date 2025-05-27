package com.gary.ChatApp.domain.service.friendRequest;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.web.dto.FriendRequestDto;
import com.gary.ChatApp.web.dto.RespondToFriendRequestDto;

import java.util.List;

public interface FriendRequestService {
    FriendRequestDto sendRequest(FriendRequestDto friendRequestDto);
    void respondToRequest(RespondToFriendRequestDto responseDto);
    List<FriendRequestDto> getPendingRequests(Long userId);
}

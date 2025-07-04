package com.gary.domain.service.friendRequest;

import com.gary.web.dto.rest.respondToFriendDto.RespondToFriendDto;
import com.gary.web.dto.rest.friendRequest.FriendRequestResponse;

import java.util.List;
import java.util.UUID;


public interface FriendRequestService {

    FriendRequestResponse sendRequest(UUID senderId, UUID receiverId);


    void respondToRequest(RespondToFriendDto responseDto, UUID userId);

    boolean existsBySenderIdAndReceiverIdAndStatusIn(UUID senderId, UUID receiverId);

    List<FriendRequestResponse> getPendingRequests(UUID userId);


    List<FriendRequestResponse> getSentRequests(UUID userId);

}

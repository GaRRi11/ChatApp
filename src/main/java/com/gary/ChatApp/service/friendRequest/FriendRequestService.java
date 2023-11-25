package com.gary.ChatApp.service.friendRequest;

import com.gary.ChatApp.storage.model.friendrequest.FriendRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface FriendRequestService {

    FriendRequest sendFriendRequest(FriendRequest friendRequest);

    FriendRequest acceptFriendRequest(FriendRequest friendRequest);

    FriendRequest declineFriendRequest(FriendRequest friendRequest);

    FriendRequest unfriend(FriendRequest friendRequest);

    boolean checkIfExists(FriendRequest friendRequest);

    boolean checkFriendStatus(Long senderId, Long receiverId);

    Optional<FriendRequest> findById(Long id);

}

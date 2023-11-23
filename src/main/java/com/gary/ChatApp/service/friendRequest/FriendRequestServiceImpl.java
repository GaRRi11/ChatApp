package com.gary.ChatApp.service.friendRequest;

import com.gary.ChatApp.service.user.UserService;
import com.gary.ChatApp.storage.model.friendrequest.FriendRequest;
import com.gary.ChatApp.storage.model.friendrequest.RequestStatus;
import com.gary.ChatApp.storage.repository.FriendRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService{

    private final FriendRequestRepository friendRequestRepository;
    private final UserService userService;


    @Override
    public FriendRequest sendFriendRequest(FriendRequest friendRequest) {
        if (!checkIfExists(friendRequest)){
            friendRequest.setRequestStatus(RequestStatus.PENDING);
            friendRequestRepository.save(friendRequest);
        }
        return friendRequest;
    }

    @Override
    public FriendRequest acceptFriendRequest(FriendRequest friendRequest) {
        friendRequest.setRequestStatus(RequestStatus.ACCEPTED);
        friendRequestRepository.save(friendRequest);
        userService.addFriend(friendRequest.getSenderId(),friendRequest.getReceiverId());
        return friendRequest;
    }

    @Override
    public FriendRequest declineFriendRequest(FriendRequest friendRequest) {
        friendRequest.setRequestStatus(RequestStatus.DECLINED);
        friendRequestRepository.save(friendRequest);
        return friendRequest;
    }

    @Override
    public boolean checkIfExists(FriendRequest friendRequest) {
        return friendRequestRepository.existsBySenderIdAndReceiverId(friendRequest.getSenderId(), friendRequest.getReceiverId());
    }

    @Override
    public Optional<FriendRequest> findById(Long id) {
        return Optional.empty();
    }
}

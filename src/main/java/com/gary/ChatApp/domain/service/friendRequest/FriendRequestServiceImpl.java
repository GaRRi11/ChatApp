package com.gary.ChatApp.domain.service.friendRequest;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;
import com.gary.ChatApp.domain.model.friendrequest.Friendship;
import com.gary.ChatApp.repository.FriendRequestRepository;
import com.gary.ChatApp.repository.FriendshipRepository;
import com.gary.ChatApp.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;

    @Override
    public FriendRequest sendRequest(Long senderId, Long receiverId) {
        return friendRequestRepository.save(
                new FriendRequest(null, senderId, receiverId, RequestStatus.PENDING)
        );
    }

    @Override
    public void respondToRequest(Long requestId, boolean accepted) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        request.setStatus(accepted ? RequestStatus.ACCEPTED : RequestStatus.DECLINED);
        friendRequestRepository.save(request);

        if (accepted) {
            friendshipRepository.save(new Friendship(null, request.getSenderId(), request.getReceiverId()));
            friendshipRepository.save(new Friendship(null, request.getReceiverId(), request.getSenderId()));
        }
    }

    @Override
    public List<FriendRequest> getPendingRequests(Long userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING);
    }
}

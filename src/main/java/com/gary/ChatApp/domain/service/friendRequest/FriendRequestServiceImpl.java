package com.gary.ChatApp.domain.service.friendRequest;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;
import com.gary.ChatApp.domain.repository.FriendRequestRepository;
import com.gary.ChatApp.domain.repository.FriendshipRepository;
import com.gary.ChatApp.domain.service.friendship.FriendshipManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
            FriendshipManager.saveBidirectional(request.getSenderId(),request.getReceiverId(),friendshipRepository);
            log.info("Friend request accepted between {} and {}", request.getSenderId(), request.getReceiverId());
        }
        else {
            log.info("Friend request declined between {} and {}", request.getSenderId(), request.getReceiverId());
        }
    }

    @Override
    public List<FriendRequest> getPendingRequests(Long userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING);
    }
}

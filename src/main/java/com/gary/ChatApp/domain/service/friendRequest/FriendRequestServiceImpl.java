package com.gary.ChatApp.domain.service.friendRequest;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;
import com.gary.ChatApp.domain.repository.FriendRequestRepository;
import com.gary.ChatApp.domain.repository.FriendshipRepository;
import com.gary.ChatApp.domain.service.friendship.FriendshipManager;
import com.gary.ChatApp.web.dto.FriendRequestDto;
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
    public FriendRequestDto sendRequest(Long senderId, Long receiverId) {
        return FriendRequestDto.fromEntity(
                friendRequestRepository.save(
                FriendRequest.builder()
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .status(RequestStatus.PENDING)
                        .build()));
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
    public List<FriendRequestDto> getPendingRequests(Long userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestDto::fromEntity)  // convert each entity to DTO
                .toList();  // collect as List<FriendRequestDto>
    }
}

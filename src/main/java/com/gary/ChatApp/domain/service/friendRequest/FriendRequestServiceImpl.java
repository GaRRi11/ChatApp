package com.gary.ChatApp.domain.service.friendRequest;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;
import com.gary.ChatApp.domain.repository.FriendRequestRepository;
import com.gary.ChatApp.domain.repository.FriendshipRepository;
import com.gary.ChatApp.domain.service.friendship.FriendshipManager;
import com.gary.ChatApp.web.dto.FriendRequestDto;
import com.gary.ChatApp.web.dto.RespondToFriendRequestDto;
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
    public FriendRequestDto sendRequest(FriendRequestDto friendRequestDto) {
        log.info("Sending friend request from {} to {}", friendRequestDto.senderId(), friendRequestDto.receiverId());
        return FriendRequestDto.fromEntity(
                friendRequestRepository.save(
                FriendRequest.builder()
                        .senderId(friendRequestDto.senderId())
                        .receiverId(friendRequestDto.receiverId())
                        .status(RequestStatus.PENDING)
                        .build()));
    }

    @Override
    public void respondToRequest(RespondToFriendRequestDto responseDto) {

        FriendRequest request = friendRequestRepository.findById(responseDto.requestId())
                .orElseThrow(() -> {
                    log.warn("Friend request not found for requestId={}", responseDto.requestId());
                    return new IllegalArgumentException("Friend request not found");
                });

        request.setStatus(responseDto.accept() ? RequestStatus.ACCEPTED : RequestStatus.DECLINED);
        friendRequestRepository.save(request);

        if (responseDto.accept()) {
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

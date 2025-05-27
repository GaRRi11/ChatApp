package com.gary.ChatApp.domain.service.friendRequest;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.model.friendrequest.RequestStatus;
import com.gary.ChatApp.domain.repository.FriendRequestRepository;
import com.gary.ChatApp.domain.repository.FriendshipRepository;
import com.gary.ChatApp.domain.service.friendship.FriendshipManager;
import com.gary.ChatApp.exceptions.FriendRequestNotFoundException;
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
    public FriendRequestDto sendRequest(FriendRequestDto dto) {
        log.info("Processing friend request from {} to {}", dto.senderId(), dto.receiverId());

        FriendRequest newRequest = FriendRequest.builder()
                .senderId(dto.senderId())
                .receiverId(dto.receiverId())
                .status(RequestStatus.PENDING)
                .build();

        FriendRequest saved = friendRequestRepository.save(newRequest);
        return FriendRequestDto.fromEntity(saved);
    }

    @Override
    public void respondToRequest(RespondToFriendRequestDto dto) {
        FriendRequest request = friendRequestRepository.findById(dto.requestId())
                .orElseThrow(() -> {
                    log.warn("Friend request not found for ID {}", dto.requestId());
                    return new FriendRequestNotFoundException(dto.requestId());
                });

        RequestStatus newStatus = dto.accept() ? RequestStatus.ACCEPTED : RequestStatus.DECLINED;
        request.setStatus(newStatus);
        friendRequestRepository.save(request);

        if (newStatus == RequestStatus.ACCEPTED) {
            FriendshipManager.saveBidirectional(request.getSenderId(), request.getReceiverId(), friendshipRepository);
            log.info("Accepted friend request between {} and {}", request.getSenderId(), request.getReceiverId());
        } else {
            log.info("Declined friend request between {} and {}", request.getSenderId(), request.getReceiverId());
        }
    }

    @Override
    public List<FriendRequestDto> getPendingRequests(Long userId) {
        log.debug("Fetching pending friend requests for userId={}", userId);
        return friendRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestDto::fromEntity)  // convert each entity to DTO
                .toList();  // collect as List<FriendRequestDto>
    }
}

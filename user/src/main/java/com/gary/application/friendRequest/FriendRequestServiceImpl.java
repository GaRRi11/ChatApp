package com.gary.application.friendRequest;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.common.time.TimeFormat;
import com.gary.application.friendship.FriendshipManager;
import com.gary.domain.model.friendrequest.FriendRequest;
import com.gary.domain.model.friendrequest.RequestStatus;
import com.gary.domain.repository.jpa.friendRequest.FriendRequestRepository;
import com.gary.domain.service.friendRequest.FriendRequestService;
import com.gary.domain.service.friendship.FriendshipService;
import com.gary.web.dto.rest.respondToFriendDto.RespondToFriendDto;
import com.gary.web.dto.rest.friendRequest.FriendRequestResponse;
import com.gary.web.exception.rest.BadRequestException;
import com.gary.web.exception.rest.DuplicateResourceException;
import com.gary.web.exception.rest.ResourceNotFoundException;
import com.gary.web.exception.rest.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipManager friendshipManager;
    private final FriendshipService  friendshipService;

    @Override
    @LoggableAction("Send Friend Request")
    @Timed("friendRequest.send.duration")
    public FriendRequestResponse sendRequest(UUID senderId, UUID receiverId) {

        if (senderId.equals(receiverId)) {
            throw new BadRequestException("Cannot send a friend request to yourself.");
        }

        if (friendshipService.areFriends(senderId, receiverId)) {
            throw new DuplicateResourceException("Friendship already exists");
        }

        boolean exists = friendRequestRepository.existsBySenderIdAndReceiverIdAndStatusIn(
                senderId,
                receiverId,
                List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED)
        );

        if (exists) {
            throw new DuplicateResourceException("Friend request already exists");
        }

        FriendRequest newRequest = FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        FriendRequest saved = friendRequestRepository.save(newRequest);

        return FriendRequestResponse.fromEntity(saved);
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    @LoggableAction("Respond Friend Request")
    @Timed("friendRequest.respond.duration")
    public void respondToRequest(RespondToFriendDto dto, UUID userId) {

        FriendRequest request = friendRequestRepository.findById(dto.requestId())
                .orElseThrow(() -> {
                    log.warn("Timestamp='{}' Friend request not found for requestId={}.",
                            TimeFormat.nowTimestamp(),
                            dto.requestId());
                    return new ResourceNotFoundException("Friend Request By Id:" + dto.requestId() + "Does not exist");
                });

        if (!request.getReceiverId().equals(userId)) {
            log.warn("Timestamp='{}' User {} is not authorized to respond to friend request {}.",
                    TimeFormat.nowTimestamp(),
                    userId,
                    dto.requestId());
            throw new UnauthorizedException("You are not authorized to respond to this friend request");
        }

        RequestStatus newStatus = dto.accept() ? RequestStatus.ACCEPTED : RequestStatus.DECLINED;
        request.setStatus(newStatus);
        request.setRespondedAt(LocalDateTime.now());

        if (newStatus == RequestStatus.ACCEPTED) {
            friendshipManager.saveBidirectional(request.getSenderId(), request.getReceiverId());
            log.info("Timestamp='{}' Accepted friend request between {} and {}.",
                    TimeFormat.nowTimestamp(),
                    request.getSenderId(),
                    request.getReceiverId());
        } else {
            log.info("Timestamp='{}' Declined friend request between {} and {}.",
                    TimeFormat.nowTimestamp(),
                    request.getSenderId(),
                    request.getReceiverId());
        }
    }


    @Override
    @LoggableAction("Get Pending Friend Requests")
    public List<FriendRequestResponse> getPendingRequests(UUID userId) {

        return friendRequestRepository
                .findByReceiverIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestResponse::fromEntity)
                .toList();

    }


    @Override
    @LoggableAction("Get Sent Friend Requests")
    public List<FriendRequestResponse> getSentRequests(UUID userId) {

        return friendRequestRepository
                .findBySenderIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestResponse::fromEntity)
                .toList();
    }


}

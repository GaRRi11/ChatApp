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
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.rest.respondToFriendDto.RespondToFriendDto;
import com.gary.web.dto.rest.friendRequest.FriendRequestResponse;
import com.gary.web.exception.rest.BadRequestException;
import com.gary.web.exception.rest.DuplicateResourceException;
import com.gary.web.exception.rest.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipManager friendshipManager;
    private final UserService userService;
    private final FriendshipService friendshipService;

    @Override
    @LoggableAction("Send Friend Request")
    @Timed("friendRequest.send.duration")
    public FriendRequestResponse sendRequest(UUID senderId, UUID receiverId) {

        if (userService.findById(receiverId).isEmpty()) {
            log.error("User with id {} not found", receiverId);
            throw new ResourceNotFoundException("User with ID " + receiverId + " not found");
        }

        if (senderId.equals(receiverId)) {
            log.warn("User {} attempted to send a friend request to themselves.", senderId);
            throw new BadRequestException("FriendRequestController: User can't send a friend request to themselves.");
        }

        if (friendshipService.areFriends(senderId, receiverId)) {
            log.warn("User {} attempted to send a friend request to already-friended user {}.", senderId, receiverId);
            throw new BadRequestException("Friend request cannot be sent. User " + receiverId + " is already in your friends list.");
        }

        if (existsBySenderIdAndReceiverIdAndStatusIn(senderId, receiverId)) {
            log.warn("User {} attempted to send a duplicate friend request to user {}.", senderId, receiverId);
            throw new DuplicateResourceException("Friend request already sent to user " + receiverId + ".");
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

        UUID requestId = dto.requestId();

        Optional<FriendRequest> optionalRequest = friendRequestRepository.findById(requestId);

        if (optionalRequest.isEmpty()) {
            log.warn("Friend request not found for requestId={}.", dto.requestId());
            throw new ResourceNotFoundException("Friend Request By Id:" + dto.requestId() + "Does not exist");
        }

        FriendRequest request = optionalRequest.get();

        if (request.getStatus() != RequestStatus.PENDING) {
            log.warn("Cannot respond to friend request {} because its status is {}.", requestId, request.getStatus());
            throw new DuplicateResourceException("This friend request has already been processed.");
        }

        if (friendshipService.areFriends(request.getSenderId(), request.getReceiverId())) {
            log.warn("Duplicate friend request: Users {} and {} are already friends.",
                    request.getSenderId(), request.getReceiverId());
            throw new DuplicateResourceException(
                    "Friend request is invalid because users are already friends."
            );
        }

        RequestStatus newStatus = dto.accept() ? RequestStatus.ACCEPTED : RequestStatus.DECLINED;
        request.setStatus(newStatus);
        request.setRespondedAt(LocalDateTime.now());

        if (newStatus == RequestStatus.ACCEPTED) {
            friendshipManager.saveBidirectional(request.getSenderId(), request.getReceiverId());
        }
    }

    @Override
    public boolean existsBySenderIdAndReceiverIdAndStatusIn(UUID senderId, UUID receiverId) {
        return friendRequestRepository.existsBySenderIdAndReceiverIdAndStatusIn(
                senderId,
                receiverId,
                List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED)
        );
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

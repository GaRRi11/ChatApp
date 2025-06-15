package com.gary.application.friendRequest;

import com.gary.domain.model.friendrequest.FriendRequest;
import com.gary.domain.model.friendrequest.RequestStatus;
import com.gary.domain.repository.friendRequest.FriendRequestRepository;
import com.gary.domain.event.friendRequestAccepted.FriendRequestAcceptedEvent;
import com.gary.domain.service.friendRequest.FriendRequestService;
import com.gary.web.exception.DuplicateResourceException;
import com.gary.web.exception.FriendRequestNotFoundException;
import com.gary.web.exception.UnauthorizedException;
import com.gary.web.dto.respondToFriendDto.RespondToFriendDto;
import com.gary.web.dto.friendRequest.FriendRequestResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public FriendRequestResponse sendRequest(UUID senderId, UUID receiverId) {


        boolean exists = friendRequestRepository.existsBySenderIdAndReceiverIdAndStatusIn(
                senderId,
                receiverId,
                List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED)
        );

        if (exists) {
            throw new DuplicateResourceException("Friend request already exists");
        }


        log.info("Processing friend request from {} to {}", senderId, receiverId);

        FriendRequest newRequest = FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        FriendRequest saved = friendRequestRepository.save(newRequest);

        FriendRequestResponse response = FriendRequestResponse.fromEntity(saved);

        return response;
    }

    @Transactional
    @Override
    public void respondToRequest(RespondToFriendDto dto, UUID userId) {
        FriendRequest request = friendRequestRepository.findById(dto.requestId())
                .orElseThrow(() -> {
                    log.warn("Friend request not found for ID {}", dto.requestId());
                    return new FriendRequestNotFoundException(dto.requestId());
                });

        if (!request.getReceiverId().equals(userId)) {
            log.warn("User {} is not authorized to respond to friend request {}", userId, dto.requestId());
            throw new UnauthorizedException("You are not authorized to respond to this friend request");
        }


        RequestStatus newStatus = dto.accept() ? RequestStatus.ACCEPTED : RequestStatus.DECLINED;
        request.setStatus(newStatus);
        request.setRespondedAt(LocalDateTime.now());
        friendRequestRepository.save(request);

        if (newStatus == RequestStatus.ACCEPTED) {
            eventPublisher.publishEvent(new FriendRequestAcceptedEvent(this, request.getSenderId(), request.getReceiverId()));
            log.info("Accepted friend request between {} and {}", request.getSenderId(), request.getReceiverId());
        } else {
            log.info("Declined friend request between {} and {}", request.getSenderId(), request.getReceiverId());
        }
    }

    @Override
    public List<FriendRequestResponse> getPendingRequests(UUID userId) {
        log.debug("Fetching pending friend requests for userId={}", userId);
        return friendRequestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestResponse::fromEntity)  // convert each entity to DTO
                .toList();  // collect as List<FriendRequestDto>
    }

    @Override
    public List<FriendRequestResponse> getSentRequests(UUID  userId) {
        log.debug("Fetching sent friend requests by userId={}", userId);
        return friendRequestRepository.findBySenderIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestResponse::fromEntity)
                .toList();
    }

}

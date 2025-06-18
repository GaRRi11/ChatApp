package com.gary.application.friendRequest;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.friendship.FriendshipManager;
import com.gary.domain.model.friendrequest.FriendRequest;
import com.gary.domain.model.friendrequest.RequestStatus;
import com.gary.domain.repository.friendRequest.FriendRequestRepository;
import com.gary.domain.service.friendRequest.FriendRequestService;
import com.gary.web.exception.DuplicateResourceException;
import com.gary.web.exception.FriendRequestNotFoundException;
import com.gary.web.exception.RespondToRequestServiceUnavailableException;
import com.gary.web.exception.UnauthorizedException;
import com.gary.web.dto.respondToFriendDto.RespondToFriendDto;
import com.gary.web.dto.friendRequest.FriendRequestResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipManager friendshipManager;
    private final MeterRegistry meterRegistry;

    @Override
    @LoggableAction("Send Friend Request")
    @Timed("friendRequest.send.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "sendRequestFallback")
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

        meterRegistry.counter("friend.request.send", "status", "success").increment();

        return response;
    }


    public FriendRequestResponse sendRequestFallback(UUID senderId, UUID receiverId, Throwable t) {
        log.warn("Failed to send friend request for {} to {}", senderId, receiverId);
        meterRegistry.counter("friend.request.send", "status", "fallback").increment();
        return FriendRequestResponse.builder()
                .id(null)
                .senderId(senderId)
                .receiverId(receiverId)
                .status(RequestStatus.FAILED)
                .createdAt(null)
                .build();
    }

    @Transactional
    @Override
    @LoggableAction("Respond Friend Request")
    @Timed("friendRequest.respond.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "respondToRequestFallback")
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
            friendshipManager.saveBidirectional(request.getSenderId(), request.getReceiverId());
            log.info("Accepted friend request between {} and {}", request.getSenderId(), request.getReceiverId());
        } else {
            log.info("Declined friend request between {} and {}", request.getSenderId(), request.getReceiverId());
        }

        meterRegistry.counter("friend.request.respond", "status", "success").increment();
    }

    public void respondToRequestFallback(RespondToFriendDto dto, UUID userId, Throwable t) {
        log.warn("Failed to respond to friend request for {} to {}", userId, dto.requestId());
        meterRegistry.counter("friend.request.respond", "status", "fallback").increment();
        throw new RespondToRequestServiceUnavailableException("Service temporarily unavailable. Please try again later.");
    }

    @Override
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getPendingRequestsFallback")
    public List<FriendRequestResponse> getPendingRequests(UUID userId) {
        log.debug("Fetching pending friend requests for userId={}", userId);

        List<FriendRequestResponse> responses = friendRequestRepository
                .findByReceiverIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestResponse::fromEntity)
                .toList();

        return responses;
    }

    public List<FriendRequestResponse> getPendingRequestsFallback(UUID userId, Throwable t) {
        log.warn("Fallback: Failed to fetch pending friend requests for userId={}", userId, t);
        return Collections.emptyList();
    }

    @Override
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getSentRequestsFallback")
    public List<FriendRequestResponse> getSentRequests(UUID userId) {
        log.debug("Fetching sent friend requests by userId={}", userId);

        List<FriendRequestResponse> responses = friendRequestRepository
                .findBySenderIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestResponse::fromEntity)
                .toList();

        return responses;
    }

    public List<FriendRequestResponse> getSentRequestsFallback(UUID userId, Throwable t) {
        log.warn("Fallback: Failed to fetch sent friend requests for userId={}", userId, t);
        return Collections.emptyList();
    }


}

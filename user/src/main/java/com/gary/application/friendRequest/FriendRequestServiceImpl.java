package com.gary.application.friendRequest;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.common.MetricIncrement;
import com.gary.application.common.TimeFormat;
import com.gary.application.friendship.FriendshipManager;
import com.gary.domain.model.friendrequest.FriendRequest;
import com.gary.domain.model.friendrequest.RequestStatus;
import com.gary.domain.repository.friendRequest.FriendRequestRepository;
import com.gary.domain.service.friendRequest.FriendRequestService;
import com.gary.web.exception.*;
import com.gary.web.dto.respondToFriendDto.RespondToFriendDto;
import com.gary.web.dto.friendRequest.FriendRequestResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipManager friendshipManager;
    private final MetricIncrement metricIncrement;

    @Override
    @Transactional
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

        FriendRequest newRequest = FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        FriendRequest saved = friendRequestRepository.save(newRequest);

        FriendRequestResponse response = FriendRequestResponse.fromEntity(saved);

        metricIncrement.incrementMetric("friend.request.send", "success");

        return response;
    }


    FriendRequestResponse sendRequestFallback(UUID senderId, UUID receiverId, Throwable t) {

        log.warn("Timestamp='{}' Failed to send friend request from senderId={} to receiverId={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                senderId,
                receiverId,
                t.toString());

        metricIncrement.incrementMetric("friend.request.send", "fallback");

        throw new SendFriendRequestServiceUnavailableException("Service temporarily unavailable. Please try again later.");
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
                    log.warn("Timestamp='{}' Friend request not found for requestId={}.",
                            TimeFormat.nowTimestamp(),
                            dto.requestId());
                    return new FriendRequestNotFoundException(dto.requestId());
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
        friendRequestRepository.save(request);

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


        metricIncrement.incrementMetric("friend.request.respond", "success");
    }


    void respondToRequestFallback(RespondToFriendDto dto, UUID userId, Throwable t) {
        log.warn("Timestamp='{}' Failed to respond to friend request from user {} to requestId={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                dto.requestId(),
                t.toString());

        metricIncrement.incrementMetric("friend.request.respond", "fallback");

        throw new RespondToRequestServiceUnavailableException("Service temporarily unavailable. Please try again later.");
    }

    @Override
    @LoggableAction("Get Pending Friend Requests")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getPendingRequestsFallback")
    public List<FriendRequestResponse> getPendingRequests(UUID userId) {

        return friendRequestRepository
                .findByReceiverIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestResponse::fromEntity)
                .toList();

    }

    List<FriendRequestResponse> getPendingRequestsFallback(UUID userId, Throwable t) {
        log.warn("Timestamp='{}' Fallback: Failed to fetch pending friend requests for userId={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                t.toString());

        throw new GetFriendRequestsServiceUnavailableException("Unable to retrieve pending friend requests. Please try again later.");
    }

    @Override
    @LoggableAction("Get Sent Friend Requests")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getSentRequestsFallback")
    public List<FriendRequestResponse> getSentRequests(UUID userId) {

        return friendRequestRepository
                .findBySenderIdAndStatus(userId, RequestStatus.PENDING).stream()
                .map(FriendRequestResponse::fromEntity)
                .toList();
    }

    List<FriendRequestResponse> getSentRequestsFallback(UUID userId, Throwable t) {
        log.warn("Timestamp='{}' Fallback: Failed to fetch sent friend requests for userId={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                t.toString());

        throw new GetFriendRequestsServiceUnavailableException("Unable to retrieve pending friend requests. Please try again later.");
    }


}

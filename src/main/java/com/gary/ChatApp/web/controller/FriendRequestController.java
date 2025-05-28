package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.service.friendRequest.FriendRequestService;
import com.gary.ChatApp.domain.service.friendship.FriendshipService;
import com.gary.ChatApp.exceptions.FriendshipAlreadyExistsException;
import com.gary.ChatApp.web.dto.FriendRequestDto;
import com.gary.ChatApp.web.dto.RespondToFriendRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend-requests")
@RequiredArgsConstructor
@Slf4j
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final FriendshipService friendshipService;

    @PostMapping("/send")
    public ResponseEntity<FriendRequestDto> sendRequest(
            @RequestBody @Valid FriendRequestDto request,
            @AuthenticationPrincipal User authenticatedUser) {

        Long senderId = authenticatedUser.getId();

        log.debug("User {} sending friend request to {}", senderId, request.receiverId());

        if (friendshipService.areFriends(senderId, request.receiverId())) {
            log.warn("Users {} and {} are already friends", senderId, request.receiverId());
            throw new FriendshipAlreadyExistsException(senderId, request.receiverId());
        }

        FriendRequestDto toSend = new FriendRequestDto(
                senderId,
                request.receiverId(),
                request.status() // probably REQUESTED or PENDING
        );

        FriendRequestDto sentRequest = friendRequestService.sendRequest(toSend);
        return ResponseEntity.ok(sentRequest);
    }

    @PostMapping("/respond")
    public ResponseEntity<Void> respondToRequest(
            @RequestBody @Valid RespondToFriendRequestDto responseDto,
            @AuthenticationPrincipal User authenticatedUser) {

        log.debug("User {} responding to friend request id={}, accept={}",
                authenticatedUser.getId(), responseDto.requestId(), responseDto.accept());

        // Optionally, verify that the authenticated user is the receiver of the friend request here
        friendRequestService.respondToRequest(responseDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendRequestDto>> getPendingRequests(
            @AuthenticationPrincipal User authenticatedUser) {

        Long userId = authenticatedUser.getId();
        log.debug("Fetching pending friend requests for userId={}", userId);
        List<FriendRequestDto> pendingRequests = friendRequestService.getPendingRequests(userId);
        return ResponseEntity.ok(pendingRequests);
    }
}

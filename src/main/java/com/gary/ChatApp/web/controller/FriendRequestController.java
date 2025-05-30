package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.service.friendRequest.FriendRequestService;
import com.gary.ChatApp.domain.service.friendship.FriendshipService;
import com.gary.ChatApp.exceptions.FriendshipAlreadyExistsException;
import com.gary.ChatApp.web.dto.respondToFriendDto.RespondToFriendDto;
import com.gary.ChatApp.web.dto.friendRequest.FriendRequestResponse;
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
    public ResponseEntity<FriendRequestResponse> sendRequest(
            @RequestParam Long receiverId,
            @AuthenticationPrincipal User authenticatedUser) {

        Long senderId = authenticatedUser.getId();

        log.debug("User {} sending friend request to {}", senderId, receiverId);

        if (friendshipService.areFriends(senderId, receiverId)) {
            log.warn("Users {} and {} are already friends", senderId, receiverId);
            throw new FriendshipAlreadyExistsException(senderId, receiverId);
        }

        FriendRequestResponse sentRequest = friendRequestService.sendRequest(senderId, receiverId);
        return ResponseEntity.ok(sentRequest);
    }

    @PostMapping("/respond")
    public ResponseEntity<Void> respondToRequest(
            @RequestBody @Valid RespondToFriendDto dto,
            @AuthenticationPrincipal User authenticatedUser) {



        log.debug("User {} responding to friend request id={}, accept={}",
                authenticatedUser.getId(), dto.requestId(), dto.accept());

        Long userId = authenticatedUser.getId();

        friendRequestService.respondToRequest(dto, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendRequestResponse>> getPendingRequests(
            @AuthenticationPrincipal User authenticatedUser) {

        Long userId = authenticatedUser.getId();
        log.debug("Fetching pending friend requests for userId={}", userId);
        List<FriendRequestResponse> pendingRequests = friendRequestService.getPendingRequests(userId);
        return ResponseEntity.ok(pendingRequests);
    }
}

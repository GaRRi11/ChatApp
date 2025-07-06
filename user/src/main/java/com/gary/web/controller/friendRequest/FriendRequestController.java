package com.gary.web.controller.friendRequest;

import com.gary.domain.model.user.User;
import com.gary.domain.service.friendRequest.FriendRequestService;
import com.gary.web.dto.rest.respondToFriendDto.RespondToFriendDto;
import com.gary.web.dto.rest.friendRequest.FriendRequestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friend-requests")
@RequiredArgsConstructor
@Slf4j
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @PostMapping("/send/{receiverId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FriendRequestResponse> sendRequest(
            @PathVariable UUID receiverId,
            @AuthenticationPrincipal User authenticatedUser) {

        UUID senderId = authenticatedUser.getId();
        FriendRequestResponse sentRequest = friendRequestService.sendRequest(senderId, receiverId);
        return ResponseEntity.ok(sentRequest);
    }


    @PostMapping("/respond")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> respondToRequest(
            @RequestBody @Valid RespondToFriendDto dto,
            @AuthenticationPrincipal User authenticatedUser) {

        UUID userId = authenticatedUser.getId();
        friendRequestService.respondToRequest(dto, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FriendRequestResponse>> getPendingRequests(
            @AuthenticationPrincipal User authenticatedUser) {

        UUID userId = authenticatedUser.getId();
        List<FriendRequestResponse> pendingRequests = friendRequestService.getPendingRequests(userId);
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FriendRequestResponse>> getSentRequests(
            @AuthenticationPrincipal User authenticatedUser) {

        UUID userId = authenticatedUser.getId();
        List<FriendRequestResponse> sentRequests = friendRequestService.getSentRequests(userId);
        return ResponseEntity.ok(sentRequests);
    }

}

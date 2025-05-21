package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.service.friendRequest.FriendRequestService;
import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.domain.model.friendrequest.FriendRequest;
import com.gary.ChatApp.web.dto.FriendRequestDTOMapper;
import com.gary.ChatApp.web.security.UserContext;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/friend")
@AllArgsConstructor
public class FriendRequestController {

    private final UserService userService;
    private final FriendRequestService friendRequestService;

    private final FriendRequestDTOMapper friendRequestDTOMapper;



    @PostMapping("/send/{id}")
    @CheckUserExistence
    public ResponseEntity<String> sendFriendRequest(@RequestParam("id") Long receiverId) {

        friendRequestService.sendFriendRequest(friendRequestDTOMapper.fromDTO(
                        UserContext.getUser().getId(),
                        receiverId
        ));
        return ResponseEntity.ok("Friend request sent successfully");
    }

    @PostMapping("/unfriend{id}")
    @CheckUserExistence
    public ResponseEntity<String> unfriend(@RequestParam("id") Long receiverId){
        friendRequestService.unfriend(friendRequestDTOMapper.fromDTO(
                UserContext.getUser().getId(),
                receiverId
        ));
        return ResponseEntity.ok("Unfriended successfully");
    }

    @PostMapping("/accept")
    @CheckUserExistence
    public ResponseEntity<String> acceptFriendRequest(@RequestParam("requestId") Long requestId) {
        // Fetch friend request by ID
        FriendRequest friendRequest = friendRequestService.findById(requestId).get(); //TODO mivxedav

        if (friendRequest == null) {
            return ResponseEntity.badRequest().body("Friend request not found");
        }

        // Accept the friend request
        friendRequestService.acceptFriendRequest(friendRequest);
        return ResponseEntity.ok("Friend request accepted");
    }

    @PostMapping("/decline")
    @CheckUserExistence
    public ResponseEntity<String> declineFriendRequest(@RequestParam("requestId") Long requestId) {
        // Fetch friend request by ID
        FriendRequest friendRequest = friendRequestService.findById(requestId).get(); //TODO mivxedav

        if (friendRequest == null) {
            return ResponseEntity.badRequest().body("Friend request not found");
        }

        // Decline the friend request
        friendRequestService.declineFriendRequest(friendRequest);
        return ResponseEntity.ok("Friend request declined");
    }
}

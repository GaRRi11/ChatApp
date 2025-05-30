package com.gary.ChatApp.domain.service.friendship;

import com.gary.ChatApp.web.dto.user.UserResponse;

import java.util.List;

public interface FriendshipService {
    List<UserResponse> getFriends(Long userId);
    boolean areFriends(Long senderId, Long receiverId);
    void removeFriend(Long userId, Long friendId);
}

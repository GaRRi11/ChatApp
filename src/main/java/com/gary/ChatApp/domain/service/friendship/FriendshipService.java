package com.gary.ChatApp.domain.service.friendship;

import java.util.List;

public interface FriendshipService {
    List<Long> getFriendIds(Long userId);
    boolean areFriends(Long senderId, Long receiverId);
    void removeFriend(Long userId, Long friendId);
}

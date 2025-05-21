package com.gary.ChatApp.domain.service.Friendship;

import java.util.List;

public interface FriendshipService {
    List<Long> getFriendIds(Long userId);
    boolean areFriends(Long userId, Long otherUserId);
    void removeFriend(Long userId, Long friendId);
}

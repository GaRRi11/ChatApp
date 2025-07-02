package com.gary.domain.service.friendship;

import com.gary.common.status.ResultStatus;
import com.gary.web.dto.rest.user.UserResponse;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {
    List<UserResponse> getFriends(UUID userId);

    boolean areFriends(UUID senderId, UUID receiverId);

    void removeFriend(UUID userId, UUID friendId);
}

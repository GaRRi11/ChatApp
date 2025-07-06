package com.gary.application.friendship;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.domain.model.friendship.Friendship;
import com.gary.domain.model.user.User;
import com.gary.domain.repository.jpa.friendship.FriendshipRepository;
import com.gary.domain.service.chat.ChatCacheService;
import com.gary.domain.service.friendship.FriendshipService;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.rest.user.UserResponse;
import com.gary.web.exception.rest.BadRequestException;
import com.gary.web.exception.rest.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserService userService;
    private final FriendshipManager friendshipManager;
    private final ChatCacheService chatCacheService;


    @Override
    @LoggableAction("Get Friends")
    @Timed("friendship.get.duration")
    public List<UserResponse> getFriends(UUID userId) {

        List<UUID> friendIds = friendshipRepository.findByUserId(userId).stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> friends = userService.findAllById(friendIds);

        return friends.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    @LoggableAction("Are Friends Check")
    public boolean areFriends(UUID senderId, UUID receiverId) {
        return friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId);
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    @LoggableAction("Remove Friends")
    public void removeFriend(UUID userId, UUID friendId) {

        if (friendId.equals(userId)) {
            log.warn("User {} attempted to remove themselves as a friend", userId);
            throw new  BadRequestException("You can't remove yourself as a friend");
        }

        if (userService.findById(friendId).isEmpty()) {
            log.debug("removeFriend - Not Found: friendId {} does not exist, requested by userId {}", friendId, userId);
            throw new ResourceNotFoundException("User with ID " + userId + " not found");
        }

        if (areFriends(userId, friendId)) {
            log.debug("removeFriend - Not Found: User {} tried to remove non-friend {}", userId, friendId);
            throw new ResourceNotFoundException("User " + userId + " is not friends with user " + friendId + ".");
        }

        friendshipManager.deleteBidirectional(userId, friendId);

        chatCacheService.clearCachedMessages(userId, friendId);

    }

}

package com.gary.domain.service.friendship;

import com.gary.domain.model.friendship.Friendship;
import com.gary.domain.model.user.User;
import com.gary.domain.repository.FriendshipRepository;
import com.gary.domain.service.cache.ChatCacheService;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.user.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserService userService;
    private final FriendshipManager friendshipManager;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    public List<UserResponse> getFriends(Long userId) {
        List<Long> friendIds = friendshipRepository.findByUserId(userId).stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());

        List<User> friends = userService.findAllById(friendIds);

        return friends.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean areFriends(Long senderId, Long receiverId) {
        return friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId);
    }

    @Transactional
    @Override
    public void removeFriend(Long userId, Long friendId) {
        try {
            friendshipManager.deleteBidirectional(userId, friendId, friendshipRepository);

            eventPublisher.publishEvent(new FriendshipRemovedEvent(this, userId, friendId));

            log.info("Removed friendship and evicted cache for userId={} and friendId={}", userId, friendId);
        } catch (RuntimeException e) {
            log.error("Failed to remove friendship between userId={} and friendId={}", userId, friendId, e);
        }
    }
}

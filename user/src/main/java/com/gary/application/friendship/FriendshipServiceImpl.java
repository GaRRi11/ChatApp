package com.gary.application.friendship;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.domain.model.friendship.Friendship;
import com.gary.domain.model.user.User;
import com.gary.domain.repository.friendship.FriendshipRepository;
import com.gary.domain.service.chat.ChatCacheService;
import com.gary.domain.service.friendship.FriendshipService;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.user.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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
    @Timed("friendship.get.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getFriendsFallback")
    public List<UserResponse> getFriends(UUID userId) {
        List<UUID> friendIds = friendshipRepository.findByUserId(userId).stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());

        List<User> friends = userService.findAllById(friendIds);

        return friends.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }


    public List<UserResponse> getFriendsFallback(UUID userId, Throwable t) {
        log.warn("Fallback triggered: Could not fetch friends for userId={}", userId, t);
        return List.of(); // or return a cached version if available
    }


    @Override
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "areFriendsFallback")
    public boolean areFriends(UUID senderId, UUID receiverId) {
        return friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId);
    }

    public boolean areFriendsFallback(UUID senderId, UUID receiverId, Throwable t) {
        log.warn("Fallback triggered: Could not check friendship between {} and {}", senderId, receiverId, t);
        return false;
    }

    @Transactional
    @Override
    public void removeFriend(UUID userId, UUID friendId) {
        try {
            friendshipManager.deleteBidirectional(userId, friendId);

            chatCacheService.clearCachedMessages(userId, friendId);

            log.info("Removed friendship and evicted cache for userId={} and friendId={}", userId, friendId);
        } catch (RuntimeException e) {
            log.error("Failed to remove friendship between userId={} and friendId={}", userId, friendId, e);
        }
    }

}

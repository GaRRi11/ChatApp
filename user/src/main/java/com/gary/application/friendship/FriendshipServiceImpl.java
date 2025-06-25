package com.gary.application.friendship;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.common.ResultStatus;
import com.gary.common.time.TimeFormat;
import com.gary.domain.model.friendship.Friendship;
import com.gary.domain.model.user.User;
import com.gary.domain.repository.jpa.friendship.FriendshipRepository;
import com.gary.domain.service.chat.ChatCacheService;
import com.gary.domain.service.friendship.FriendshipService;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.rest.user.UserResponse;
import com.gary.web.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @LoggableAction("Get Friends")
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


    List<UserResponse> getFriendsFallback(UUID userId, Throwable t) {

        log.warn("Timestamp='{}' Fallback triggered: Could not fetch friends for userId={}. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                t.toString(),
                t);

        throw new ServiceUnavailableException("Failed to fetch friends for userId=" + userId);
    }


    @Override
    @LoggableAction("Are Friends Check")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "areFriendsFallback")
    public ResultStatus areFriends(UUID senderId, UUID receiverId) {

        if (friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId)) {
            return ResultStatus.HIT;
        } else {
            return ResultStatus.MISS;
        }

    }

    ResultStatus areFriendsFallback(UUID senderId, UUID receiverId, Throwable t) {

        log.warn("Timestamp='{}' Fallback triggered: Could not check friendship between {} and {}. Cause: {}",
                TimeFormat.nowTimestamp(),
                senderId,
                receiverId,
                t.toString(),
                t);

        return ResultStatus.FALLBACK;
    }

    @Transactional
    @Override
    @LoggableAction("Remove Friends")
    public void removeFriend(UUID userId, UUID friendId) {
        try {
            friendshipManager.deleteBidirectional(userId, friendId);

            chatCacheService.clearCachedMessages(userId, friendId);

        } catch (RuntimeException e) {
            log.error("Timestamp='{}' Failed to remove friendship between userId={} and friendId={}. Cause: {}",
                    TimeFormat.nowTimestamp(),
                    userId,
                    friendId,
                    e,
                    e);

            throw new ServiceUnavailableException("Failed to fetch friends for userId=" + userId);
        }
    }

}

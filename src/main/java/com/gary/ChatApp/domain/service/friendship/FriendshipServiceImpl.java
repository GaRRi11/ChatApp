package com.gary.ChatApp.domain.service.friendship;

import com.gary.ChatApp.domain.model.friendship.Friendship;
import com.gary.ChatApp.domain.repository.FriendshipRepository;
import com.gary.ChatApp.domain.service.chatCacheService.ChatCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final ChatCacheService chatCacheService; // 👈 Add this


    @Override
    public List<Long> getFriendIds(Long userId) {
        return friendshipRepository.findByUserId(userId).stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean areFriends(Long senderId, Long receiverId) {
        return friendshipRepository.findByUserIdAndFriendId(senderId, receiverId).isPresent();
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        try {
            // Assuming FriendshipManager.deleteBidirectional handles both sides removal
            FriendshipManager.deleteBidirectional(userId, friendId, friendshipRepository);

            // Evict chat cache to avoid stale data
            chatCacheService.evictChatCache(userId, friendId);

            log.info("Removed friendship and evicted cache for userId={} and friendId={}", userId, friendId);
        } catch (Exception e) {
            log.error("Failed to remove friendship between userId={} and friendId={}", userId, friendId, e);
            // Optionally rethrow or handle exception depending on your error handling strategy
        }
    }
}

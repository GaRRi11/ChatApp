package com.gary.ChatApp.domain.service.friendship;

import com.gary.ChatApp.domain.model.friendship.Friendship;
import com.gary.ChatApp.domain.repository.FriendshipRepository;
import com.gary.ChatApp.domain.service.chatCacheService.ChatCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
        FriendshipManager.deleteBidirectional(userId,friendId,friendshipRepository);
        chatCacheService.evictChatCache(userId, friendId); // 👈 Evict from Redis
    }
}

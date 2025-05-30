package com.gary.domain.service.friendship;


import com.gary.ChatApp.domain.repository.FriendshipRepository;
import com.gary.ChatApp.domain.model.friendship.Friendship;
import org.springframework.stereotype.Component;

import java.util.List;

public class FriendshipManager {
    public static void saveBidirectional(Long userId1, Long userId2, FriendshipRepository repository) {
        List<Friendship> friendships = List.of(
                new Friendship(null, userId1, userId2),
                new Friendship(null, userId2, userId1)
        );
        repository.saveAll(friendships);
    }

    public static void deleteBidirectional(Long userId1, Long userId2, FriendshipRepository repository) {
        repository.deleteByUserIdAndFriendId(userId1, userId2);
        repository.deleteByUserIdAndFriendId(userId2, userId1);
    }
}


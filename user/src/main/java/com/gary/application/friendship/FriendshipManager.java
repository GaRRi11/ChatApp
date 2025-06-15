package com.gary.application.friendship;


import com.gary.domain.repository.friendship.FriendshipRepository;
import com.gary.domain.model.friendship.Friendship;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class FriendshipManager {

    public void saveBidirectional(UUID userId1, UUID userId2, FriendshipRepository repository) {
        List<Friendship> friendships = List.of(
                new Friendship(null, userId1, userId2),
                new Friendship(null, userId2, userId1)
        );
        repository.saveAll(friendships);
    }

    public void deleteBidirectional(UUID userId1, UUID userId2, FriendshipRepository repository) {
        repository.deleteByUserIdAndFriendId(userId1, userId2);
        repository.deleteByUserIdAndFriendId(userId2, userId1);
    }
}

